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
import discord4j.voice.*
import io.facet.discord.*
import io.facet.discord.commands.*
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

    val audioPlayerManager = config.audioPlayerManager

    private val schedulers = ConcurrentHashMap<Snowflake, TrackScheduler>()

    internal val voiceConnections = ConcurrentHashMap<Snowflake, VoiceConnection>()

    val commands = listOf(
        JoinCommand,
        LeaveCommand,
        NowPlayingCommand,
        PauseResumeCommand,
        PlayCommand,
        SkipCommand,
        VolumeCommand,
        VoteSkipCommand,
        YouTubeSearch
    )

    suspend fun initializeFor(client: GatewayDiscordClient, guildId: Snowflake) {
        if (schedulers.containsKey(guildId))
            return

        val scheduler = TrackScheduler(client, audioPlayerManager.createPlayer())
            .also { schedulers[guildId] = it }

        val settings = sql {
            MusicQueue.select(MusicQueue.whereGuildIs(guildId)).forEach { row ->
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

            MusicQueue.deleteWhere(op = MusicQueue.whereGuildIs(guildId))

            MusicSettings.select(MusicSettings.whereGuildIs(guildId))
                .first()
                .let { Triple(it[MusicSettings.loop], it[MusicSettings.autoplay], it[MusicSettings.volume]) }
        }

        lastConnectedChannelFor(guildId)?.let { channelId ->
            val vc = client.getChannelById(channelId).await() as VoiceChannel
            vc.join { it.setProvider(scheduler.audioProvider) }.await()
        }

        val (loop, autoplay, volume) = settings
        if (loop)
            scheduler.toggleShouldLoop()
        if (autoplay)
            scheduler.toggleShouldAutoplay()
        scheduler.player.volume = volume
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

    suspend fun volumeFor(guildId: Snowflake): Int = sql {
        MusicSettings.select(MusicSettings.whereGuildIs(guildId))
            .firstOrNull()
            ?.let { it[MusicSettings.volume] }
            ?: somethingIsWrong("volume", guildId)
    }

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
            .also { it ?: somethingIsWrong("currentlyConnectedChannel", guildId) }!!
            .let { it[MusicSettings.lastConnectedChannel] }
    }

    suspend fun updateLastConnectedChannelFor(guildId: Snowflake, channelId: Snowflake?) {
        sql {
            MusicSettings.update(MusicSettings.whereGuildIs(guildId)) {
                it[lastConnectedChannel] = channelId
            }
        }
    }

    suspend fun shouldLoopFor(guildId: Snowflake): Boolean = sql {
        MusicSettings.select(MusicSettings.whereGuildIs(guildId))
            .firstOrNull()
            ?.let { it[MusicSettings.loop] }
            ?: somethingIsWrong("loop", guildId)
    }

    suspend fun toggleShouldLoopFor(guildId: Snowflake) {
        val scheduler = trackSchedulerFor(guildId)
        val loop = scheduler.toggleShouldLoop()
        sql {
            MusicSettings.update(MusicSettings.whereGuildIs(guildId)) {
                it[MusicSettings.loop] = loop
            }
        }
    }

    suspend fun shouldAutoplayFor(guildId: Snowflake): Boolean = sql {
        MusicSettings.select(MusicSettings.whereGuildIs(guildId))
            .firstOrNull()
            ?.let { it[MusicSettings.autoplay] }
            ?: somethingIsWrong("autoplay", guildId)
    }

    suspend fun toggleShouldAutoplayFor(guildId: Snowflake) {
        val scheduler = trackSchedulerFor(guildId)
        val autoplay = scheduler.toggleShouldAutoplay()
        sql {
            MusicSettings.update(MusicSettings.whereGuildIs(guildId)) {
                it[MusicSettings.autoplay] = autoplay
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
                    else -> cont.resume(playlist.tracks.take(policy.size.coerceAtMost(playlist.tracks.size)))
                }
            }

            override fun noMatches() =  cont.resume(listOf())
            override fun loadFailed(e: FriendlyException) = cont.resumeWithException(
                IllegalStateException("LOAD_FAILED: $query", e)
            )
        })
    }

    suspend fun deinitializeFor(guildId: Snowflake) {
        schedulers.remove(guildId)?.let { scheduler ->
            logger.debug("Saving queue state for guild with id $guildId")
            sql {
                MusicQueue.batchInsert(scheduler.allTracks()) { track ->
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
        sql {
            logger.info("Adding music settings entry to database for guild $guildId")
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
        requiredFeatures = listOf(PostgresDatabase,  GuildStorage, ChatCommands)
    ) {

        override fun install(client: GatewayDiscordClient, configuration: Config.() -> Unit): Music {
            runBlocking { sql { create(MusicSettings, MusicQueue) } }

            return Music(Config().apply(configuration)).also { feature ->
                feature.audioPlayerManager.configuration.frameBufferFactory = AudioFrameBufferFactory { a, b, c ->
                    NonAllocatingAudioFrameBuffer(a, b, c)
                }

                AudioSourceManagers.registerRemoteSources(feature.audioPlayerManager)
                AudioSourceManagers.registerLocalSource(feature.audioPlayerManager)

                client.listener<VoiceStateUpdateEvent> {
                    if (current.userId == client.selfId && current.channelId.isPresent)
                        feature.updateLastConnectedChannelFor(current.guildId, current.channelId.get())
                }

                client.listener<GuildDeleteEvent> {
                    logger.info("Deinitializing guild: $guildId")
                    if (!isUnavailable)
                        feature.deinitializeFor(guildId)
                }

                client.feature(ChatCommands).apply { feature.commands.forEach { registerCommand(it) } }

                client.feature(GuildStorage).addTaskOnGuildInitialization { event ->
                    val guildId = event.guild.id
                    if (!feature.hasGuild(guildId))
                        feature.insertNewRow(guildId)

                    if (feature.isEnabledFor(guildId))
                        feature.initializeFor(client, guildId)
                }

                client.feature(PostgresDatabase).addShutdownTask {
                    coroutineScope {
                        feature.voiceConnections.entries
                            .map { (guildId, vc) ->
                                async {
                                    logger.info("Disconnecting from channel in guild with id $guildId")
                                    vc.disconnect()
                                }
                            }
                            .forEach { it.await() }

                        feature.schedulers.keys.forEach { feature.deinitializeFor(it) }

                        feature.audioPlayerManager.shutdown()
                    }
                }
            }
        }
    }
}
