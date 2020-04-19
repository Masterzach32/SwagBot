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
import discord4j.voice.*
import io.facet.core.extensions.*
import io.facet.discord.*
import io.facet.discord.commands.*
import io.facet.discord.extensions.*
import org.jetbrains.exposed.sql.*
import reactor.core.publisher.*
import xyz.swagbot.*
import xyz.swagbot.database.*
import xyz.swagbot.features.*
import xyz.swagbot.features.guilds.*
import xyz.swagbot.features.music.commands.*
import xyz.swagbot.features.music.tables.*
import java.time.*
import java.util.concurrent.*

class Music(config: Config, private val guildStorage: GuildStorage) {

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

    fun initializeFor(client: DiscordClient, guildId: Snowflake) {
        if (guildStorage.hasGuild(guildId) && !schedulers.containsKey(guildId))
            schedulers[guildId] = TrackScheduler(client, audioPlayerManager.createPlayer())

        val scheduler = trackSchedulerFor(guildId)
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
        }.let { (loop, autoplay, volume) ->
            if (loop)
                scheduler.toggleShouldLoop()
            if (autoplay)
                scheduler.toggleShouldAutoplay()
            scheduler.player.volume = volume
        }

        currentlyConnectedChannelFor(guildId)
            .map { channelId ->
                logger.debug("Reconnecting to last connected voice channel: $channelId")
                client.getChannelById(channelId)
                    .cast<VoiceChannel>()
                    .flatMap { vc ->
                        vc.join { it.setProvider(trackSchedulerFor(vc.guildId).audioProvider) }
                    }
            }
            .orElse(Mono.empty())
            .subscribe()
    }

    fun trackSchedulerFor(guildId: Snowflake) = schedulers[guildId]!!

    fun volumeFor(guildId: Snowflake) = sql {
        MusicSettings.select(MusicSettings.whereGuildIs(guildId))
            .firstOrNull()
            ?.let { it[MusicSettings.volume] }
            ?: somethingIsWrong("volume", guildId)
    }

    fun updateVolumeFor(guildId: Snowflake, volume: Int) {
        trackSchedulerFor(guildId).player.volume = volume
        sql {
            MusicSettings.update(MusicSettings.whereGuildIs(guildId)) {
                it[MusicSettings.volume] = volume
            }
        }
    }

    fun currentlyConnectedChannelFor(guildId: Snowflake) = sql {
        MusicSettings.select(MusicSettings.whereGuildIs(guildId))
            .firstOrNull()
            ?.let { it[MusicSettings.currentlyConnectedChannel].toOptional().map { id -> id.toSnowflake() } }
            ?: somethingIsWrong("currentlyConnectedChannel", guildId)
    }

    fun updateCurrentlyConnectedChannelFor(guildId: Snowflake, channelId: Snowflake?) = sql {
        MusicSettings.update(MusicSettings.whereGuildIs(guildId)) {
            it[currentlyConnectedChannel] = channelId?.asLong()
        }
    }

    fun shouldLoopFor(guildId: Snowflake) = sql {
        MusicSettings.select(MusicSettings.whereGuildIs(guildId))
            .firstOrNull()
            ?.let { it[MusicSettings.loop] }
            ?: somethingIsWrong("loop", guildId)
    }

    fun toggleShouldLoopFor(guildId: Snowflake) =  trackSchedulerFor(guildId)
        .toggleShouldLoop()
        .also { shouldLoop ->
            sql {
                MusicSettings.update(MusicSettings.whereGuildIs(guildId)) {
                    it[loop] = shouldLoop
                }
            }
        }

    fun shouldAutoplayFor(guildId: Snowflake) = sql {
        MusicSettings.select(MusicSettings.whereGuildIs(guildId))
            .firstOrNull()
            ?.let { it[MusicSettings.autoplay] }
            ?: somethingIsWrong("autoplay", guildId)
    }

    fun toggleShouldAutoplayFor(guildId: Snowflake) = trackSchedulerFor(guildId)
        .toggleShouldAutoplay()
        .also { shouldAutoplay ->
            sql {
                MusicSettings.update(MusicSettings.whereGuildIs(guildId)) {
                    it[autoplay] = shouldAutoplay
                }
            }
        }

    fun search(query: String, maxResults: Int = -1, callback: (results: List<AudioTrack>) -> Unit) {
        audioPlayerManager.loadItem(query, object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) = callback(listOf(track))

            override fun playlistLoaded(playlist: AudioPlaylist) {
                if (!playlist.isSearchResult)
                    return

                if (maxResults == -1)
                    callback(playlist.tracks)
                else
                    callback(playlist.tracks.subList(0, maxResults.coerceAtMost(playlist.tracks.size)))
            }

            override fun noMatches() = callback(emptyList())
            override fun loadFailed(e: FriendlyException) = callback(emptyList())
        })
    }

    private fun somethingIsWrong(variableName: String, guildId: Snowflake): Nothing {
        throw IllegalStateException("Could not access $variableName for $guildId")
    }

    fun hasGuild(guildId: Snowflake) = sql {
        MusicSettings.select(MusicSettings.whereGuildIs(guildId)).firstOrNull() != null
    }

    class Config {
        val audioPlayerManager: AudioPlayerManager = DefaultAudioPlayerManager()
    }

    companion object : DiscordClientFeature<Config, Music>(
        "music",
        listOf(SystemInteraction, GuildStorage, ChatCommands)
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

                client.feature(GuildStorage).addTaskOnInitialization { event ->
                    if (!feature.hasGuild(event.guild.id)) {
                        logger.info("Adding music settings for entry to database for ${event.guild.id} ")
                        sql { MusicSettings.insert { it[guildId] = event.guild.id.asLong() } }
                    }

                    feature.initializeFor(event.client, event.guild.id)
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

                client.feature(SystemInteraction).addShutdownTask {
                    logger.info("Disconnecting from connected voice channels.")

                    Flux.fromIterable(feature.voiceConnections.toList())
                        .map { (guildId, vc) ->
                            logger.debug("Disconnecting from channel in guild with id $guildId")
                            vc.disconnect()
                        }
                        .blockLast(Duration.ofSeconds(10))

                    logger.info("Saving queue state to database.")
                    feature.schedulers.forEach { (guildId, scheduler) ->
                        logger.debug("Saving queue state for guild with id $guildId")
                        sql {
                            MusicQueue.batchInsert(scheduler.allTracks()) {
                                logger.debug("Inserting track into queue database: ${it.identifier}")
                                this[MusicQueue.guildId] = guildId.asLong()
                                this[MusicQueue.identifier] = it.identifier
                                this[MusicQueue.requesterId] = it.trackContext.requester.asLong()
                                this[MusicQueue.requestedChannelId] = it.trackContext.requester.asLong()
                            }
                        }
                        logger.debug("Stopping current track.")
                        scheduler.player.stopTrack()
                    }

                    logger.info("Shutting down AudioPlayerManager.")
                    feature.audioPlayerManager.shutdown()
                }
            }
        }
    }
}
