package xyz.swagbot.features.music

import com.sedmelluq.discord.lavaplayer.format.*
import com.sedmelluq.discord.lavaplayer.player.*
import com.sedmelluq.discord.lavaplayer.track.playback.*
import discord4j.voice.*
import java.nio.*

class LPAudioProvider(
    val player: AudioPlayer
) : AudioProvider(ByteBuffer.allocate(StandardAudioDataFormats.DISCORD_OPUS.maximumChunkSize())) {

    val frame = MutableAudioFrame()

    init {
        frame.setBuffer(buffer)
    }

    override fun provide(): Boolean {
        return player.provide(frame).also { didProvide ->
            if (didProvide)
                buffer.flip()
        }
    }
}
