package xyz.swagbot.features.music

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import discord4j.common.util.Snowflake
import xyz.swagbot.extensions.setTrackContext
import xyz.swagbot.logger

class SilentAudioTrackLoadHandler(
    private val scheduler: TrackScheduler,
    private val requesterId: Snowflake,
    private val requestedChannelId: Snowflake
) : AudioLoadResultHandler {

    override fun trackLoaded(track: AudioTrack) {
        track.setTrackContext(requesterId, requestedChannelId)
        scheduler.queue(track)
    }

    override fun playlistLoaded(playlist: AudioPlaylist) {
        playlist.tracks.forEach {
            it.setTrackContext(requesterId, requestedChannelId)
            scheduler.queue(it)
        }
    }

    override fun noMatches() {}

    override fun loadFailed(e: FriendlyException) {
        logger.warn("Exception while loading track from database.", e)
    }
}