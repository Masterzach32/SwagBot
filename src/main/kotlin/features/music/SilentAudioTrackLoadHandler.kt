package xyz.swagbot.features.music

import com.sedmelluq.discord.lavaplayer.player.*
import com.sedmelluq.discord.lavaplayer.tools.*
import com.sedmelluq.discord.lavaplayer.track.*
import discord4j.core.`object`.util.*

class SilentAudioTrackLoadHandler(
    val scheduler: TrackScheduler,
    val requesterId: Snowflake,
    val requestedChannelId: Snowflake
) : AudioLoadResultHandler {

    override fun trackLoaded(track: AudioTrack) {
        track.userData = TrackContext(requesterId, requestedChannelId)
        scheduler.queue(track)
    }

    override fun playlistLoaded(playlist: AudioPlaylist) {
        playlist.tracks.forEach {
            it.userData = TrackContext(requesterId, requestedChannelId)
            scheduler.queue(it)
        }
    }

    override fun noMatches() {

    }

    override fun loadFailed(exception: FriendlyException) {
        if (exception.severity != FriendlyException.Severity.COMMON)
            exception.printStackTrace()
    }
}