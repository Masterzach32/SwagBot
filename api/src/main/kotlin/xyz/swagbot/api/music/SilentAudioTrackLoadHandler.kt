package xyz.swagbot.api.music

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import sx.blah.discord.handle.obj.IUser
import xyz.swagbot.logger

class SilentAudioTrackLoadHandler(val player: TrackHandler, val user: IUser) : AudioLoadResultHandler {

    override fun loadFailed(exception: FriendlyException) {
        logger.warn("Could not load track: ${exception.message}")
    }

    override fun trackLoaded(track: AudioTrack) {
        track.userData = TrackUserData(user)
        player.queue(track)
        logger.debug("Loaded track: ${track.identifier}")
    }

    override fun noMatches() {
        logger.warn("Could not find a match for track.")
    }

    override fun playlistLoaded(playlist: AudioPlaylist) {
        for (track in playlist.tracks) {
            track.userData = TrackUserData(user)
            player.queue(track)
            logger.debug("Loaded track: ${track.identifier}")
        }
    }
}