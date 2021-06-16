package xyz.swagbot.features.music

import com.sedmelluq.discord.lavaplayer.player.*
import com.sedmelluq.discord.lavaplayer.player.event.*
import com.sedmelluq.discord.lavaplayer.tools.*
import com.sedmelluq.discord.lavaplayer.track.*
import discord4j.common.util.*
import discord4j.core.*
import discord4j.gateway.*
import xyz.swagbot.*
import xyz.swagbot.extensions.*
import java.util.*
import java.util.concurrent.*

class TrackScheduler(
    private val client: GatewayDiscordClient,
    shardInfo: ShardInfo,
    val player: AudioPlayer
) : AudioEventAdapter() {

    val audioProvider = LPAudioProvider(player)

    var shouldLoop = false
        internal set(value) {
            field = if (shouldAutoplay) false else value
        }

    var shouldAutoplay = false
        internal set(value) {
            field = value

            if (shouldAutoplay)
                shouldLoop = false
        }


    private val _queue: Queue<AudioTrack> = LinkedBlockingQueue()

    val queue: List<AudioTrack>
        get() = _queue.toList()

    val allTracks: List<AudioTrack>
        get() = listOfNotNull(player.playingTrack) + queue

    val currentTrackTimeLeft: Long
        get() = player.playingTrack?.let { currentTrack ->
            currentTrack.info.length - currentTrack.position
        } ?: 0L

    val queueTimeLeft: Long
        get() = queue.fold(0L) { acc, track -> acc + track.info.length } + currentTrackTimeLeft

    init {
        player.addListener(this)
    }

    fun queue(track: AudioTrack) {
        if (!player.startTrack(track, true))
            _queue.add(track)
    }

    fun playNext(): AudioTrack? {
        val oldTrack = player.playingTrack
        if (shouldLoop && oldTrack != null) {
            val clone = oldTrack.makeClone()
            clone.userData = oldTrack.userData
            queue(clone)
        }

        if (_queue.isNotEmpty())
            player.startTrack(_queue.poll(), false)
        else if (player.playingTrack != null)
            player.stopTrack()

//        if (shouldAutoplay && queue.isEmpty())
//            getAndQueueAutoplayTrack()

        return oldTrack
    }

    fun clearQueue() = _queue.clear()

    fun pruneQueue(users: Set<Snowflake>) = queue
        .filter { it.context.requesterId !in users }
        .onEach { _queue.remove(it) }

    override fun onTrackStart(player: AudioPlayer, track: AudioTrack) {

    }

    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        if (endReason.mayStartNext)
            playNext()
    }

    override fun onTrackException(player: AudioPlayer, track: AudioTrack, exception: FriendlyException) {
        logger.warn("Encountered error with track: ${track.info.uri}", exception)

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

    @Deprecated("Use property accessor syntax")
    fun toggleShouldLoop(): Boolean {
        shouldLoop = !shouldLoop

        if (shouldAutoplay)
            shouldLoop = false

        return shouldLoop
    }

    @Deprecated("Use property accessor syntax")
    fun toggleShouldAutoplay(): Boolean {
        shouldAutoplay = !shouldAutoplay

        if (shouldAutoplay)
            shouldLoop = false

//        if (shouldAutoplay && player.playingTrack == null)
//            getAndQueueAutoplayTrack()

        return shouldAutoplay
    }
}
