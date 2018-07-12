package xyz.swagbot.api.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import net.masterzach32.commands4k.AdvancedMessageBuilder
import sx.blah.discord.handle.obj.IGuild
import sx.blah.discord.handle.obj.IUser
import sx.blah.discord.util.RequestBuffer
import xyz.swagbot.Stats
import xyz.swagbot.audioPlayerManager
import xyz.swagbot.dsl.getFormattedTitle
import xyz.swagbot.dsl.getRequester
import xyz.swagbot.dsl.getTrackPreferences
import xyz.swagbot.dsl.getTrackUserData
import xyz.swagbot.logger

class TrackHandler(val guild: IGuild, val player: AudioPlayer) : AudioEventAdapter() {

    private val queue = mutableListOf<AudioTrack>()

    val audioProvider = AudioProvider(player)

    var shouldLoop = false
        private set
    var shouldAutoplay = false
        private set

    fun queue(track: AudioTrack) {
        if (!player.startTrack(track, true))
            queue.add(track)
    }

    fun playNext(): AudioTrack? {
        val oldTrack = player.playingTrack
        if (shouldLoop && oldTrack != null) {
            val clone = oldTrack.makeClone()
            clone.userData = oldTrack.userData
            queue(clone)
        }

        if (queue.isNotEmpty())
            player.startTrack(queue.removeAt(0), false)
        else if (player.playingTrack != null)
            player.stopTrack()

        if (shouldAutoplay && queue.isEmpty())
            getAndQueueAutoplayTrack()

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

    fun getQueueBrowser() = QueueBrowser(queue)

    fun shuffleQueue() {
        queue.shuffle()
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
        val removed = queue.filter { !users.contains(it.getRequester()) }
        removed.forEach { queue.remove(it) }
        return removed
    }

    fun toggleShouldLoop(): Boolean {
        shouldLoop = !shouldLoop

        if (shouldAutoplay)
            shouldLoop = false

        return shouldLoop
    }

    fun toggleShouldAutoplay(): Boolean {
        shouldAutoplay = !shouldAutoplay

        if (shouldAutoplay)
            shouldLoop = false

        if (shouldAutoplay && player.playingTrack == null)
            getAndQueueAutoplayTrack()

        return shouldAutoplay
    }

    fun getQueueLength(): Long {
        var count = 0L
        queue.forEach { count += it.info.length }
        return count
    }

    override fun onTrackStart(player: AudioPlayer, track: AudioTrack) {
        Stats.TRACKS_PLAYED.addStat()
    }

    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        if (endReason.mayStartNext)
            playNext()
    }

    override fun onTrackException(player: AudioPlayer, track: AudioTrack?, exception: FriendlyException) {
        logger.warn("An audio track encountered an exception: ${exception.message}")
        exception.printStackTrace()
        if (track != null) {
            RequestBuffer.request {
                AdvancedMessageBuilder(track.getTrackUserData().requester.orCreatePMChannel)
                        .withContent("A track you queued seems to have thrown an error: ${track.getFormattedTitle()}. " +
                                "If you continue to encounter this error, consider opening an issue on the Github page.")
                        .build()
            }
        }
        playNext()
    }

    override fun onTrackStuck(player: AudioPlayer, track: AudioTrack, thresholdMs: Long) {
        logger.warn("An audio track is stuck, skipping...")
        logger.warn("Skipped ${track.identifier}")
        playNext()
    }

    private fun getAndQueueAutoplayTrack() {
        val map = guild.client.ourUser.getVoiceStateForGuild(guild).channel.getTrackPreferences()

        if (map.isNotEmpty()) {
            var totalWeight = 0
            map.forEach { totalWeight += it.value }

            var randomTrack: String? = null
            var random = Math.random() * map.keys.size

            for (track in map.keys) {
                random -= map.getOrDefault(track, 1)
                if (random <= 0) {
                    randomTrack = track
                    break
                }
            }

            audioPlayerManager.loadItemOrdered(this, randomTrack,
                    SilentAudioTrackLoadHandler(this, guild.client.ourUser))
            Stats.TRACKS_AUTOPLAYED.addStat()
        }
    }
}