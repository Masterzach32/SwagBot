package xyz.swagbot.features.music

import com.sedmelluq.discord.lavaplayer.player.*
import com.sedmelluq.discord.lavaplayer.source.*
import com.sedmelluq.discord.lavaplayer.tools.*
import com.sedmelluq.discord.lavaplayer.track.*
import com.sedmelluq.discord.lavaplayer.track.playback.*
import discord4j.core.*
import discord4j.core.`object`.entity.*
import discord4j.core.`object`.util.*
import discord4j.core.event.domain.*
import discord4j.core.event.domain.guild.*
import discord4j.voice.*
import io.facet.core.extensions.*
import io.facet.discord.*
import io.facet.discord.commands.*
import io.facet.discord.extensions.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactor.*
import org.jetbrains.exposed.sql.*
import reactor.core.publisher.*
import xyz.swagbot.*
import xyz.swagbot.extensions.*
import xyz.swagbot.features.guilds.*
import xyz.swagbot.features.music.commands.*
import xyz.swagbot.features.music.listeners.*
import xyz.swagbot.features.music.tables.*
import xyz.swagbot.features.system.*
import java.util.concurrent.*

class Music private constructor(config: Config, private val guildStorage: GuildStorage) {

    val audioPlayerManager = config.audioPlayerManager

    private val schedulers = ConcurrentHashMap<Snowflake, TrackScheduler>()

    internal val voiceConnections = ConcurrentHashMap<Snowflake, VoiceConnection>()

    val commands = listOf(
        JoinCommand,
        LeaveCommand,
        PauseResumeCommand,
        PlayCommand,
        SkipCommand,
        VolumeCommand,
        VoteSkipCommand,
        YouTubeSearch
    )

    suspend fun initializeFor(client: DiscordClient, guildId: Snowflake) {
        if (!(guildStorage.hasGuild(guildId) && !schedulers.containsKey(guildId)))
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
                        row[MusicQueue.requesterId].toSnowflake(),
                        row[MusicQueue.requestedChannelId].toSnowflake()
                    )
                )
            }

            MusicQueue.deleteWhere(op = MusicQueue.whereGuildIs(guildId))

            MusicSettings.select(MusicSettings.whereGuildIs(guildId))
                .first()
                .let { Triple(it[MusicSettings.loop], it[MusicSettings.autoplay], it[MusicSettings.volume]) }
        }

        val vc = client.getChannelById(currentlyConnectedChannelFor(guildId)).await() as VoiceChannel
        coroutineScope {
            async {
                vc.join { it.setProvider(scheduler.audioProvider) }.await()
            }
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

    suspend fun currentlyConnectedChannelFor(guildId: Snowflake): Snowflake? = sql {
        MusicSettings.select(MusicSettings.whereGuildIs(guildId))
            .firstOrNull()
            ?.let { it[MusicSettings.currentlyConnectedChannel]?.toSnowflake() }
            ?: somethingIsWrong("currentlyConnectedChannel", guildId)
    }

    suspend fun updateCurrentlyConnectedChannelFor(guildId: Snowflake, channelId: Snowflake?) {
        sql {
            MusicSettings.update(MusicSettings.whereGuildIs(guildId)) {
                it[currentlyConnectedChannel] = channelId?.asLong()
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
    ): List<AudioTrack> = Mono.create<List<AudioTrack>> { emitter ->
        audioPlayerManager.loadItem(query, object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) = emitter.success(listOf(track))

            override fun playlistLoaded(playlist: AudioPlaylist) {
                if (policy is SearchResultPolicy.Unlimited)
                    emitter.success(playlist.tracks)
                else
                    emitter.success(playlist.tracks.take(policy.size.coerceAtMost(playlist.tracks.size)))
            }

            override fun noMatches() = emitter.success(listOf())
            override fun loadFailed(e: FriendlyException) = emitter.error(Exception("LOAD_FAILED: $query", e))
        })
    }.await()

    suspend fun deinitializeFor(guildId: Snowflake) {
        schedulers.remove(guildId)?.let { scheduler ->
            logger.debug("Saving queue state for guild with id $guildId")
            sql {
                MusicQueue.batchInsert(scheduler.allTracks()) { track ->
                    logger.debug("Inserting track into queue database: ${track.identifier}")
                    this[MusicQueue.guildId] = guildId.asLong()
                    this[MusicQueue.identifier] = track.identifier
                    this[MusicQueue.requesterId] = track.trackContext.requesterId.asLong()
                    this[MusicQueue.requestedChannelId] = track.trackContext.requestedChannelId.asLong()
                }
            }
            coroutineScope {
                scheduler.player.stopTrack()
            }
        }
    }

    private fun somethingIsWrong(variableName: String, guildId: Snowflake): Nothing {
        throw IllegalStateException("Could not access $variableName for $guildId")
    }

    private suspend fun insertNewRow(guildId: Snowflake) {
        sql {
            logger.info("Adding music settings entry to database for guild $guildId")
            MusicSettings.insert { it[MusicSettings.guildId] = guildId.asLong() }
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
                if (size <= 1)
                    throw IllegalStateException("Search result size must be larger than 1")
            }
        }
    }

    class Config {
        val audioPlayerManager: AudioPlayerManager = DefaultAudioPlayerManager()
    }

    companion object : DiscordClientFeature<Config, Music>(
        keyName = "music",
        requiredFeatures = listOf(SystemInteraction, GuildStorage, ChatCommands)
    ) {

        override fun install(client: DiscordClient, configuration: Config.() -> Unit): Music {
            runBlocking { sql { create(MusicSettings, MusicQueue) } }

            return Music(Config().apply(configuration), client.feature(GuildStorage)).also { feature ->
                feature.audioPlayerManager.configuration.frameBufferFactory = AudioFrameBufferFactory { a, b, c ->
                    NonAllocatingAudioFrameBuffer(a, b, c)
                }

                AudioSourceManagers.registerRemoteSources(feature.audioPlayerManager)
                AudioSourceManagers.registerLocalSource(feature.audioPlayerManager)

                val list = listOf(VoiceStateListener(feature), GuildLeaveListener(feature))

                client.feature(ChatCommands).apply { feature.commands.forEach { registerCommand(it) } }

                client.feature(GuildStorage).addTaskOnGuildInitialization { event ->
                    val guildId = event.guild.id
                    if (!feature.hasGuild(guildId))
                        feature.insertNewRow(guildId)

                    if (feature.isEnabledFor(guildId))
                        feature.initializeFor(client, guildId)
                }

                client.feature(SystemInteraction).addShutdownTask {
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
