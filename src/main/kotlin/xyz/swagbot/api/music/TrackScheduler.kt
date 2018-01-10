package xyz.swagbot.api.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import sx.blah.discord.handle.obj.IUser
import xyz.swagbot.dsl.getTrackUserData
import xyz.swagbot.logger
import java.util.*

class TrackScheduler(val player: AudioPlayer) : AudioEventAdapter() {

    private val queue = mutableListOf<AudioTrack>()

    val audioProvider = AudioProvider(player)

    private var shouldLoop = false

    fun queue(track: AudioTrack) {
        if (!player.startTrack(track, true))
            queue.add(track)
    }

    fun playNext(): AudioTrack? {
        val oldTrack = player.playingTrack
        if (queue.isNotEmpty())
            player.startTrack(queue.removeAt(0), false)
        else if (player.playingTrack != null)
            player.stopTrack()
        if (shouldLoop) {
            val clone = oldTrack.makeClone()
            clone.userData = oldTrack.userData
            queue(clone)
        }
        return oldTrack
    }

    fun removeTrack(index: Int): AudioTrack? {
        if (index >= queue.size)
            return null
        return queue.removeAt(index)
    }

    fun moveTrack(start: Int, final: Int): AudioTrack {
        val tmp = queue.removeAt(start)
        queue.add(final, tmp)
        return tmp
    }

    fun removeDuplicates(): Int {
        val list = mutableListOf<AudioTrack>()
        val toRemove = mutableListOf<AudioTrack>()

        queue.forEach { track ->
            if (list.any { track.identifier == it.identifier })
                toRemove.add(track)
            else
                list.add(track)
        }

        toRemove.forEach { queue.remove(it) }

        return toRemove.size
    }

    fun getQueue(): List<AudioTrack> {
        return queue
    }

    fun shuffleQueue() {
        Collections.shuffle(queue)
    }

    fun clearQueue() {
        queue.removeAll { true }
    }

    fun skipTo(index: Int): List<AudioTrack> {
        val removed = mutableListOf<AudioTrack>()
        for (i in 0 until Math.min(index - 1, queue.size-1))
            removed.add(queue.removeAt(0))
        playNext()
        return removed
    }

    fun pruneTracks(users: List<IUser>): List<AudioTrack> {
        val removed = queue.filter { !users.contains(it.getTrackUserData().author) }
        removed.forEach { queue.remove(it) }
        return removed
    }

    fun toggleShouldLoop(): Boolean {
        shouldLoop = !shouldLoop
        return shouldLoop
    }

    override fun onTrackStart(player: AudioPlayer, track: AudioTrack) {

    }

    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        if (endReason.mayStartNext)
            playNext()
    }

    override fun onTrackException(player: AudioPlayer, track: AudioTrack?, exception: FriendlyException) {
        logger.warn("An audio track encountered an exception: ${exception.message}")
        exception.printStackTrace()
    }

    override fun onTrackStuck(player: AudioPlayer, track: AudioTrack, thresholdMs: Long) {
        logger.warn("An audio track is stuck, skipping...")
        logger.warn("Skipped ${track.identifier}")
        playNext()
    }
}