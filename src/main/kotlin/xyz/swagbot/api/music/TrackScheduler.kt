package xyz.swagbot.api.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason

class TrackScheduler(val player: AudioPlayer) : AudioEventAdapter() {

    private val queue = mutableListOf<AudioTrack>()

    val audioProvider = AudioProvider(player)

    fun queue(track: AudioTrack) {
        if (!player.startTrack(track, true))
            queue.add(track)
    }

    fun playNext() {
        player.startTrack(queue.removeAt(0), false)
    }

    override fun onTrackStart(player: AudioPlayer, track: AudioTrack) {

    }

    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        if (endReason.mayStartNext)
            playNext()
    }

    override fun onTrackException(player: AudioPlayer, track: AudioTrack?, exception: FriendlyException) {

    }

    override fun onTrackStuck(player: AudioPlayer, track: AudioTrack?, thresholdMs: Long) {

    }

    fun getQueue(): List<AudioTrack> {
        return queue
    }
}