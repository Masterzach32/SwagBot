package xyz.swagbot.features.music

import com.sedmelluq.discord.lavaplayer.player.*
import com.sedmelluq.discord.lavaplayer.tools.*
import com.sedmelluq.discord.lavaplayer.track.*
import discord4j.common.util.*
import xyz.swagbot.*
import xyz.swagbot.extensions.*

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