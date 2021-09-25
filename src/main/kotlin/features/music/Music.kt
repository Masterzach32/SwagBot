package xyz.swagbot.features.music

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioItem
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrameBufferFactory
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer
import discord4j.common.util.Snowflake
import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.entity.channel.VoiceChannel
import discord4j.core.event.EventDispatcher
import discord4j.core.event.domain.VoiceStateUpdateEvent
import discord4j.core.event.domain.guild.GuildDeleteEvent
import discord4j.gateway.ShardInfo
import io.facet.common.awaitNullable
import io.facet.common.listener
import io.facet.common.unwrap
import io.facet.core.EventDispatcherFeature
import io.facet.core.feature
import io.facet.core.features.ChatCommands
import io.facet.exposed.create
import io.facet.exposed.sql
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.*
import xyz.swagbot.extensions.context
import xyz.swagbot.extensions.joinWithAutoDisconnect
import xyz.swagbot.features.guilds.GuildStorage
import xyz.swagbot.features.music.tables.MusicQueue
import xyz.swagbot.features.music.tables.MusicSettings
import xyz.swagbot.features.system.PostgresDatabase
import xyz.swagbot.logger
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class Music private constructor(config: Config) {

    private val audioPlayerManager = config.audioPlayerManager

    private val schedulers = ConcurrentHashMap<Snowflake, TrackScheduler>()

    suspend fun initializeFor(client: GatewayDiscordClient, shardInfo: ShardInfo, guildId: Snowflake) {
        if (schedulers.containsKey(guildId))
            return

        val scheduler = TrackScheduler(client, shardInfo, audioPlayerManager.createPlayer())
        schedulers[guildId] = scheduler

        coroutineScope {
            launch {
                sql { MusicQueue.select(MusicQueue.whereGuildIs(guildId)).toList() }.forEach { row ->
                    logger.debug("Loading track from queue database: ${row[MusicQueue.identifier]}")
                    audioPlayerManager.loadItemOrdered(
                        scheduler,
                        row[MusicQueue.identifier],
                        SilentAudioTrackLoadHandler(
                            scheduler,
                            row[MusicQueue.requesterId],
                            row[MusicQueue.requestedChannelId]
                        )
                    )
                }

                sql { MusicQueue.deleteWhere(op = MusicQueue.whereGuildIs(guildId)) }
            }

            val settingsRow = sql {
                MusicSettings.select(MusicSettings.whereGuildIs(guildId)).first()
            }

            launch {
                settingsRow[MusicSettings.lastConnectedChannel]?.let { channelId ->
                    client.getChannelById(channelId).awaitNullable() as? VoiceChannel
                }?.joinWithAutoDisconnect()
            }

            scheduler.shouldLoop = settingsRow[MusicSettings.loop]
            scheduler.shouldAutoplay = settingsRow[MusicSettings.autoplay]
            scheduler.player.volume = settingsRow[MusicSettings.volume]
        }
    }

    fun trackSchedulerFor(guildId: Snowflake) = schedulers[guildId] ?: somethingIsWrong("trackScheduler", guildId)

    suspend fun isEnabledFor(guildId: Snowflake): Boolean = sql {
        MusicSettings.select(MusicSettings.whereGuildIs(guildId))
            .firstOrNull()
            ?.let { it[MusicSettings.enabled] }
            ?: somethingIsWrong("isEnabled", guildId)
    }

    suspend fun updateIsEnabledFor(guildId: Snowflake, featureEnabled: Boolean) {
        sql {
            MusicSettings.update(MusicSettings.whereGuildIs(guildId)) {
                it[enabled] = featureEnabled
            }
        }
    }

    fun getVolumeFor(guildId: Snowflake): Int = trackSchedulerFor(guildId).player.volume

    suspend fun updateVolumeFor(guildId: Snowflake, volume: Int) {
        trackSchedulerFor(guildId).player.volume = volume
        sql {
            MusicSettings.update(MusicSettings.whereGuildIs(guildId)) {
                it[MusicSettings.volume] = volume
            }
        }
    }

    suspend fun lastConnectedChannelFor(guildId: Snowflake): Snowflake? = sql {
        MusicSettings.select(MusicSettings.whereGuildIs(guildId))
            .firstOrNull()
            .let { it ?: somethingIsWrong("currentlyConnectedChannel", guildId) }
            .let { it[MusicSettings.lastConnectedChannel] }
    }

    suspend fun updateLastConnectedChannelFor(guildId: Snowflake, channelId: Snowflake?) {
        sql {
            MusicSettings.update(MusicSettings.whereGuildIs(guildId)) {
                it[lastConnectedChannel] = channelId
            }
        }
    }

    fun shouldLoopFor(guildId: Snowflake): Boolean = trackSchedulerFor(guildId).shouldLoop

    suspend fun toggleShouldLoopFor(guildId: Snowflake, loop: Boolean) {
        val scheduler = trackSchedulerFor(guildId)
        scheduler.shouldLoop = loop
        sql {
            MusicSettings.update(MusicSettings.whereGuildIs(guildId)) {
                it[MusicSettings.loop] = scheduler.shouldLoop
            }
        }
    }

    fun shouldAutoplayFor(guildId: Snowflake): Boolean = trackSchedulerFor(guildId).shouldAutoplay

    suspend fun setShouldAutoplayFor(guildId: Snowflake, autoplay: Boolean) {
        val scheduler = trackSchedulerFor(guildId)
        scheduler.shouldAutoplay = autoplay
        sql {
            MusicSettings.update(MusicSettings.whereGuildIs(guildId)) {
                it[MusicSettings.autoplay] = scheduler.shouldAutoplay
            }
        }
    }

    suspend fun search(query: String): AudioTrack? = search(query, SearchResultPolicy.Single).firstOrNull()

    suspend fun search(
        query: String,
        policy: SearchResultPolicy
    ): List<AudioTrack> = suspendCoroutine { cont ->
        audioPlayerManager.loadItem(query, object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) = cont.resume(listOf(track))

            override fun playlistLoaded(playlist: AudioPlaylist) {
                when (policy) {
                    is SearchResultPolicy.Unlimited -> cont.resume(playlist.tracks)
                    else -> cont.resume(playlist.tracks.take(policy.size))
                }
            }

            override fun noMatches() = cont.resume(listOf())
            override fun loadFailed(e: FriendlyException) = cont.resumeWithException(e)
        })
    }

    suspend fun searchItem(query: String): AudioItem? = suspendCoroutine { cont ->
        audioPlayerManager.loadItem(query, object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) = cont.resume(track)

            override fun playlistLoaded(playlist: AudioPlaylist) = cont.resume(playlist)

            override fun noMatches() = cont.resume(null)
            override fun loadFailed(e: FriendlyException) = cont.resumeWithException(e)
        })
    }

    suspend fun deinitializeFor(guildId: Snowflake) {
        schedulers.remove(guildId)?.let { scheduler ->
            logger.debug("Saving queue state for guild with id $guildId")
            sql {
                MusicQueue.batchInsert(scheduler.allTracks) { track ->
                    logger.debug("Inserting track into queue database: ${track.identifier}")
                    this[MusicQueue.guildId] = guildId
                    this[MusicQueue.identifier] = track.identifier
                    this[MusicQueue.requesterId] = track.context.requesterId
                    this[MusicQueue.requestedChannelId] = track.context.requestedChannelId
                }
            }

            scheduler.player.stopTrack()
        }
    }

    private fun somethingIsWrong(variableName: String, guildId: Snowflake): Nothing {
        error("Could not access $variableName for $guildId")
    }

    private suspend fun insertNewRow(guildId: Snowflake) {
        logger.info("Adding music settings entry to database for guild $guildId")
        sql {
            MusicSettings.insert { it[MusicSettings.guildId] = guildId }
        }
    }

    suspend fun hasGuild(guildId: Snowflake): Boolean = sql {
        MusicSettings.select(MusicSettings.whereGuildIs(guildId)).firstOrNull() != null
    }

    sealed class SearchResultPolicy(val size: Int) {
        object Unlimited : SearchResultPolicy(-1)
        object Single : SearchResultPolicy(1)
        class Limited(size: Int) : SearchResultPolicy(size) {
            init {
                require(size > 1) { "Search result size must be larger than 1" }
            }
        }
    }

    class Config {
        val audioPlayerManager: AudioPlayerManager = DefaultAudioPlayerManager()
    }

    companion object : EventDispatcherFeature<Config, Music>(
        keyName = "music",
        requiredFeatures = listOf(PostgresDatabase, GuildStorage, ChatCommands)
    ) {

        override suspend fun EventDispatcher.install(scope: CoroutineScope, configuration: Config.() -> Unit): Music {
            sql { create(MusicSettings, MusicQueue) }

            return Music(Config().apply(configuration)).apply {
                audioPlayerManager.configuration.frameBufferFactory = AudioFrameBufferFactory { a, b, c ->
                    NonAllocatingAudioFrameBuffer(a, b, c)
                }

                AudioSourceManagers.registerRemoteSources(audioPlayerManager)
                AudioSourceManagers.registerLocalSource(audioPlayerManager)

                listener<VoiceStateUpdateEvent>(scope) { event ->
                    if (event.current.userId == event.client.selfId)
                        updateLastConnectedChannelFor(event.current.guildId, event.current.channelId.unwrap())
                }

                listener<GuildDeleteEvent>(scope) { event ->
                    logger.info("Deinitializing guild: ${event.guildId}")
                    if (!event.isUnavailable)
                        deinitializeFor(event.guildId)
                }

                feature(GuildStorage).addTaskOnGuildInitialization { event ->
                    val guildId = event.guild.id
                    if (!hasGuild(guildId))
                        insertNewRow(guildId)

                    if (isEnabledFor(guildId))
                        initializeFor(event.client, event.shardInfo, guildId)
                }

                feature(PostgresDatabase).addShutdownTask {
                    coroutineScope {
                        schedulers.keys.toList()
                            .forEach {
                                launch { deinitializeFor(it) }
                            }
                    }

                    audioPlayerManager.shutdown()
                }
            }
        }
    }
}
