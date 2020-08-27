package xyz.swagbot.features.music

import com.sedmelluq.discord.lavaplayer.player.*
import com.sedmelluq.discord.lavaplayer.player.event.*
import com.sedmelluq.discord.lavaplayer.tools.*
import com.sedmelluq.discord.lavaplayer.track.*
import discord4j.core.*
import xyz.swagbot.*
import java.util.*
import java.util.concurrent.*

class TrackScheduler(val client: GatewayDiscordClient, val player: AudioPlayer) : AudioEventAdapter() {

    val audioProvider = LPAudioProvider(player)

    var shouldLoop = false
    var shouldAutoplay = false

    private val queue: Queue<AudioTrack> = LinkedBlockingQueue()

    init {
        player.addListener(this)
    }

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
            player.startTrack(queue.poll(), false)
        else if (player.playingTrack != null)
            player.stopTrack()

//        if (shouldAutoplay && queue.isEmpty())
//            getAndQueueAutoplayTrack()

        return oldTrack
    }

    fun queueTimeLeft() = getQueue()
        .map { it.info.length }
        .let { if (it.isEmpty()) 0 else it.reduce { acc, l -> acc + l } } + currentTrackTimeLeft()

    fun currentTrackTimeLeft() = player.playingTrack?.let { currentTrack ->
        currentTrack.info.length - currentTrack.position
    } ?: 0L

    fun getQueue() = queue.toList()

    fun allTracks(): List<AudioTrack> = player.playingTrack
        ?.let { getQueue().toMutableList().apply { add(0, it) } }
        ?: getQueue()

    fun clearQueue() = queue.clear()

    override fun onTrackStart(player: AudioPlayer?, track: AudioTrack?) {

    }

    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        if (endReason.mayStartNext)
            playNext()
    }

    override fun onTrackException(player: AudioPlayer, track: AudioTrack, exception: FriendlyException) {
        logger.warn("Encountered error with track: ${track.info.uri}")
        exception.printStackTrace()

//        runBlocking {
//            val channel = client.getChannelById(track.context.requestedChannelId).await() as MessageChannel
//            channel.createEmbed(errorTemplate.andThen {
//                it.setDescription()
//            })
//        }
    }

    override fun onTrackStuck(player: AudioPlayer, track: AudioTrack, thresholdMs: Long) {
        logger.warn("An audio track is stuck, skipping: ${track.info.uri}")
        playNext()
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

//        if (shouldAutoplay && player.playingTrack == null)
//            getAndQueueAutoplayTrack()

        return shouldAutoplay
    }
}
