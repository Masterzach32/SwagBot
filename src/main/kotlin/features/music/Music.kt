package xyz.swagbot.features.music

import com.sedmelluq.discord.lavaplayer.player.*
import com.sedmelluq.discord.lavaplayer.source.*
import com.sedmelluq.discord.lavaplayer.tools.*
import com.sedmelluq.discord.lavaplayer.track.*
import com.sedmelluq.discord.lavaplayer.track.playback.*
import discord4j.common.util.*
import discord4j.core.*
import discord4j.core.`object`.entity.channel.*
import discord4j.core.event.domain.*
import discord4j.core.event.domain.guild.*
import io.facet.core.extensions.*
import io.facet.discord.*
import io.facet.discord.commands.*
import io.facet.discord.event.*
import io.facet.discord.exposed.*
import io.facet.discord.extensions.*
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.*
import xyz.swagbot.*
import xyz.swagbot.extensions.*
import xyz.swagbot.features.guilds.*
import xyz.swagbot.features.music.commands.*
import xyz.swagbot.features.music.tables.*
import xyz.swagbot.features.system.*
import java.util.concurrent.*
import kotlin.coroutines.*

class Music private constructor(config: Config) {

    private val audioPlayerManager = config.audioPlayerManager

    private val schedulers = ConcurrentHashMap<Snowflake, TrackScheduler>()

    suspend fun initializeFor(client: GatewayDiscordClient, guildId: Snowflake) {
        if (schedulers.containsKey(guildId))
            return

        val scheduler = TrackScheduler(client, audioPlayerManager.createPlayer())
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

            launch {
                val row = sql {
                    MusicSettings.select(MusicSettings.whereGuildIs(guildId)).first()
                }

                launch {
                    row[MusicSettings.lastConnectedChannel]?.let { channelId ->
                        client.getChannelById(channelId).awaitNullable() as? VoiceChannel
                    }?.join(this)
                }

                scheduler.shouldLoop = row[MusicSettings.loop]
                scheduler.shouldAutoplay = row[MusicSettings.autoplay]
                scheduler.player.volume = row[MusicSettings.volume]
            }
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
        sql {
            MusicSettings.update(MusicSettings.whereGuildIs(guildId)) {
                it[MusicSettings.volume] = volume
            }
        }
        trackSchedulerFor(guildId).player.volume = volume
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

    suspend fun loadItem(query: String): AudioItem? = suspendCoroutine { cont ->
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

    companion object : DiscordClientFeature<Config, Music>(
        keyName = "music",
        requiredFeatures = listOf(PostgresDatabase, GuildStorage, ChatCommands)
    ) {

        override fun install(client: GatewayDiscordClient, configuration: Config.() -> Unit): Music {
            runBlocking {
                sql { create(MusicSettings, MusicQueue) }
            }

            return Music(Config().apply(configuration)).apply {
                audioPlayerManager.configuration.frameBufferFactory = AudioFrameBufferFactory { a, b, c ->
                    NonAllocatingAudioFrameBuffer(a, b, c)
                }

                AudioSourceManagers.registerRemoteSources(audioPlayerManager)
                AudioSourceManagers.registerLocalSource(audioPlayerManager)

                BotScope.listener<VoiceStateUpdateEvent>(client) { event ->
                    if (event.current.userId == client.selfId)
                        updateLastConnectedChannelFor(event.current.guildId, event.current.channelId.value)
                }

                BotScope.listener<GuildDeleteEvent>(client) { event ->
                    logger.info("Deinitializing guild: ${event.guildId}")
                    if (!event.isUnavailable)
                        deinitializeFor(event.guildId)
                }

                client.feature(ChatCommands).registerCommands(
                    Clear,
                    JoinCommand,
                    LeaveCommand,
                    LeaverClear,
                    NowPlayingCommand,
                    PauseResumeCommand,
                    Play,
                    Premium,
                    Queue,
                    SkipCommand,
                    VolumeCommand,
                    VoteSkipCommand,
                    YouTubeSearch
                )

                client.feature(GuildStorage).addTaskOnGuildInitialization { event ->
                    val guildId = event.guild.id
                    if (!hasGuild(guildId))
                        insertNewRow(guildId)

                    if (isEnabledFor(guildId))
                        initializeFor(client, guildId)
                }

                client.feature(PostgresDatabase).addShutdownTask {
                    schedulers.keys.toList().forEach { deinitializeFor(it) }

                    audioPlayerManager.shutdown()
                }
            }
        }
    }
}
