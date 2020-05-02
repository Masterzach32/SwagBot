package xyz.swagbot.features.music

import com.sedmelluq.discord.lavaplayer.player.*
import com.sedmelluq.discord.lavaplayer.tools.*
import com.sedmelluq.discord.lavaplayer.track.*
import discord4j.core.`object`.entity.*
import xyz.swagbot.extensions.*
import xyz.swagbot.util.*
import java.time.*

class AudioTrackLoadHandler(
    val scheduler: TrackScheduler,
    val requester: Member,
    val channel: MessageChannel
) : AudioLoadResultHandler {

    override fun trackLoaded(track: AudioTrack) {
        track.userData = TrackContext(requester.id, channel.id)
        scheduler.queue(track)

        channel.createEmbed(trackRequestedTemplate(requester.displayName, track)).subscribe()
    }

    override fun playlistLoaded(playlist: AudioPlaylist) {
        if (playlist.isSearchResult && playlist.tracks.isNotEmpty())
            return playlist.tracks.first().let { track ->
                track.userData = TrackContext(requester.id, channel.id)
                channel.createEmbed(trackRequestedTemplate(requester.displayName, track).andThen { spec ->
                    requester.client
                        .commandPrefixFor(requester.guildId)
                        .let { prefix ->
                            spec.setFooter("You can search and pick results from youtube using ${prefix}search.", null)
                        }
                }).subscribe()
                scheduler.queue(track)
            }
        playlist.tracks.forEach {
            it.userData = TrackContext(requester.id, channel.id)
            scheduler.queue(it)
        }

        channel.createEmbed { embed ->
            embed.setColor(BLUE)
            embed.setDescription("${requester.displayName} queued playlist: **${playlist.name}** with" +
                    " **${playlist.tracks.size}** tracks.")
            embed.setTimestamp(Instant.now())
        }.subscribe()
    }

    override fun noMatches() {
        channel.createEmbed { embed ->
            embed.setColor(RED)
            embed.setDescription("Sorry, I could not load your track. Perhaps the video is region locked in the US," +
                    " or your url is incorrect.")
            embed.setTimestamp(Instant.now())
        }.subscribe()
    }

    override fun loadFailed(exception: FriendlyException) {
        if (exception.severity != FriendlyException.Severity.COMMON)
            exception.printStackTrace()
        channel.createEmbed { embed ->
            embed.setColor(RED)
            embed.setDescription("Could not load track due to error: ${exception.message}")
            embed.setTimestamp(Instant.now())
        }.subscribe()
    }
}
