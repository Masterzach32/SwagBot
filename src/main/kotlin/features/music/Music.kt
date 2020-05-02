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
import org.jetbrains.exposed.sql.*
import reactor.core.publisher.*
import xyz.swagbot.*
import xyz.swagbot.database.*
import xyz.swagbot.extensions.*
import xyz.swagbot.features.*
import xyz.swagbot.features.guilds.*
import xyz.swagbot.features.music.commands.*
import xyz.swagbot.features.music.tables.*
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
        VolumeCommand,
        YouTubeSearch
    )

    fun initializeFor(client: DiscordClient, guildId: Snowflake): Mono<Void> = guildStorage.hasGuild(guildId)
        .filter { it && !schedulers.containsKey(guildId) }
        .map { schedulers[guildId] = TrackScheduler(client, audioPlayerManager.createPlayer()) }
        .map { trackSchedulerFor(guildId) }
        .flatMap { scheduler ->
            sql {
                MusicQueue.select(MusicQueue.whereGuildIs(guildId))
                    .also { if (!it.empty()) logger.debug("Found tracks in queue database for guild with id $guildId") }
                    .forEach { row ->
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
                    .map { Triple(it[MusicSettings.loop], it[MusicSettings.autoplay], it[MusicSettings.volume])  }
                    .first()
            }.flatMap { (loop, autoplay, volume) ->
                val setLoop = loop.toMono()
                    .filter { it }
                    .map { scheduler.toggleShouldLoop() }
                    .then()

                val setAutoplay = autoplay.toMono()
                    .filter { it }
                    .map { scheduler.toggleShouldAutoplay() }
                    .then()

                val setVolume = volume.toMono()
                    .map { scheduler.player.volume = it }
                    .then()

                setLoop.then(setAutoplay).then(setVolume)
            }.then(
                currentlyConnectedChannelFor(guildId)
                    .flatMap { client.getChannelById(it) }
                    .cast<VoiceChannel>()
                    .flatMap { vc -> vc.join { it.setProvider(scheduler.audioProvider) } }
                    .then()
            )
        }
        .then()

    fun trackSchedulerFor(guildId: Snowflake) = schedulers[guildId] ?: somethingIsWrong("trackScheduler", guildId)

    fun volumeFor(guildId: Snowflake): Mono<Int> = sql {
        MusicSettings.select(MusicSettings.whereGuildIs(guildId))
            .firstOrNull()
            ?.let { it[MusicSettings.volume] }
            ?: somethingIsWrong("volume", guildId)
    }

    fun updateVolumeFor(guildId: Snowflake, volume: Int): Mono<Void> {
        trackSchedulerFor(guildId).player.volume = volume
        return sql {
            MusicSettings.update(MusicSettings.whereGuildIs(guildId)) {
                it[MusicSettings.volume] = volume
            }
        }.then()
    }

    fun currentlyConnectedChannelFor(guildId: Snowflake): Mono<Snowflake> = sql {
        MusicSettings.select(MusicSettings.whereGuildIs(guildId))
            .firstOrNull()
            ?.let { it[MusicSettings.currentlyConnectedChannel]?.toSnowflake().toOptional() }
    }.flatMap { optional ->
        optional?.let { Mono.justOrEmpty(it) } ?: somethingIsWrong("currentlyConnectedChannel", guildId)
    }

    fun updateCurrentlyConnectedChannelFor(guildId: Snowflake, channelId: Snowflake?): Mono<Void> = sql {
        MusicSettings.update(MusicSettings.whereGuildIs(guildId)) {
            it[currentlyConnectedChannel] = channelId?.asLong()
        }
    }.then()

    fun shouldLoopFor(guildId: Snowflake): Mono<Boolean> = sql {
        MusicSettings.select(MusicSettings.whereGuildIs(guildId))
            .firstOrNull()
            ?.let { it[MusicSettings.loop] }
            ?: somethingIsWrong("loop", guildId)
    }

    fun toggleShouldLoopFor(guildId: Snowflake): Mono<Void> = trackSchedulerFor(guildId)
        .toggleShouldLoop()
        .let { shouldLoop ->
            sql {
                MusicSettings.update(MusicSettings.whereGuildIs(guildId)) {
                    it[loop] = shouldLoop
                }
            }.then()
        }

    fun shouldAutoplayFor(guildId: Snowflake): Mono<Boolean> = sql {
        MusicSettings.select(MusicSettings.whereGuildIs(guildId))
            .firstOrNull()
            ?.let { it[MusicSettings.autoplay] }
            ?: somethingIsWrong("autoplay", guildId)
    }

    fun toggleShouldAutoplayFor(guildId: Snowflake): Mono<Void> = trackSchedulerFor(guildId)
        .toggleShouldAutoplay()
        .let { shouldAutoplay ->
            sql {
                MusicSettings.update(MusicSettings.whereGuildIs(guildId)) {
                    it[autoplay] = shouldAutoplay
                }
            }.then()
        }

    fun search(query: String, maxResults: Int = -1): Mono<List<AudioTrack>> = Mono.create { emitter ->
        audioPlayerManager.loadItem(query, object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) = emitter.success(listOf(track))

            override fun playlistLoaded(playlist: AudioPlaylist) {
                if (!playlist.isSearchResult)
                    return

                if (maxResults == -1)
                    emitter.success(playlist.tracks)
                else
                    emitter.success(playlist.tracks.take(maxResults.coerceAtMost(playlist.tracks.size)))
            }

            override fun noMatches() = emitter.error(Exception("NO_MATCHES: $query"))
            override fun loadFailed(e: FriendlyException) = emitter.error(Exception("LOAD_FAILED: $query", e))
        })
    }

    private fun deinitializeFor(guildId: Snowflake): Mono<Void> {
        return schedulers.remove(guildId)!!.toMono()
            .flatMap { scheduler ->
                logger.debug("Saving queue state for guild with id $guildId")
                sql {
                    MusicQueue.batchInsert(scheduler.allTracks()) { track ->
                        logger.debug("Inserting track into queue database: ${track.identifier}")
                        this[MusicQueue.guildId] = guildId.asLong()
                        this[MusicQueue.identifier] = track.identifier
                        this[MusicQueue.requesterId] = track.trackContext.requesterId.asLong()
                        this[MusicQueue.requestedChannelId] = track.trackContext.requestedChannelId.asLong()
                    }
                }.then(scheduler.player.stopTrack().toMono())
            }
            .then()
    }

    fun isEnabledFor(guildId: Snowflake) = sql {
        MusicSettings.select(MusicSettings.whereGuildIs(guildId))
            .firstOrNull()
            ?.let { it[MusicSettings.enabled] }
            ?: somethingIsWrong("isEnabled", guildId)
    }

    private fun somethingIsWrong(variableName: String, guildId: Snowflake): Nothing {
        throw IllegalStateException("Could not access $variableName for $guildId")
    }

    private fun insertNewRow(guildId: Snowflake): Mono<Void> = sql {
        logger.info("Adding music settings entry to database for guild $guildId")
        MusicSettings.insert { it[MusicSettings.guildId] = guildId.asLong() }
    }.then()

    fun hasGuild(guildId: Snowflake) = sql {
        MusicSettings.select(MusicSettings.whereGuildIs(guildId)).firstOrNull() != null
    }

    class Config {
        val audioPlayerManager: AudioPlayerManager = DefaultAudioPlayerManager()
    }

    companion object : DiscordClientFeature<Config, Music>(
        keyName = "music",
        requiredFeatures = listOf(SystemInteraction, GuildStorage, ChatCommands)
    ) {

        override fun install(client: DiscordClient, configuration: Config.() -> Unit): Music {
            sql { create(MusicSettings, MusicQueue) }

            return Music(Config().apply(configuration), client.feature(GuildStorage)).also { feature ->
                feature.audioPlayerManager.configuration.frameBufferFactory = AudioFrameBufferFactory { a, b, c ->
                    NonAllocatingAudioFrameBuffer(a, b, c)
                }

                AudioSourceManagers.registerRemoteSources(feature.audioPlayerManager)
                AudioSourceManagers.registerLocalSource(feature.audioPlayerManager)

                client.feature(ChatCommands).dispatcher.apply { feature.commands.forEach { it.register(this) } }

                client.feature(GuildStorage).addTaskOnGuildInitialization { event ->
                    val handleNewGuild = feature.hasGuild(event.guild.id)
                        .filter { !it }
                        .flatMap { feature.insertNewRow(event.guild.id) }
                        .then()

                    val initializeGuildPlayer = feature.isEnabledFor(event.guild.id)
                        .filter { it }
                        .flatMap { feature.initializeFor(client, event.guild.id) }
                        .then()

                    handleNewGuild.then(initializeGuildPlayer)
                }

                client.listen<VoiceStateUpdateEvent>()
                    .map { it.current }
                    .flatMap { vs ->
                        vs.user
                            .filter { it.id == it.client.selfId.get() }
                            .filter { vs.channelId.isPresent }
                            .map { feature.updateCurrentlyConnectedChannelFor(vs.guildId, vs.channelId.get()) }
                    }
                    .subscribe()

                // handle bot leaving guild
                client.listen<GuildDeleteEvent>()
                    .filter { !it.isUnavailable }
                    .flatMap { feature.deinitializeFor(it.guildId) }
                    .subscribe()

                client.feature(SystemInteraction).addShutdownTask {
                    feature.voiceConnections.entries.toFlux()
                        .map { (guildId, vc) ->
                            logger.debug("Disconnecting from channel in guild with id $guildId")
                            vc.disconnect()
                        }
                        .then(feature.schedulers.keys.toFlux().flatMap { feature.deinitializeFor(it) }.then())
                        .then(feature.audioPlayerManager.shutdownAsync())
                        .then()
                }
            }
        }
    }
}
