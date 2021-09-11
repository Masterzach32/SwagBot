package xyz.swagbot.features.music

import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame
import discord4j.voice.AudioProvider
import java.nio.ByteBuffer

class LPAudioProvider(
    private val player: AudioPlayer
) : AudioProvider(ByteBuffer.allocate(StandardAudioDataFormats.DISCORD_OPUS.maximumChunkSize())) {

    private val frame = MutableAudioFrame()

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
