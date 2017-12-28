package xyz.swagbot.api.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame
import sx.blah.discord.handle.audio.AudioEncodingType
import sx.blah.discord.handle.audio.IAudioProvider

class AudioProvider(private val player: AudioPlayer) : IAudioProvider {

    private var lastFrame: AudioFrame? = null

    override fun isReady(): Boolean {
        if (lastFrame == null)
            lastFrame = player.provide()

        return lastFrame != null
    }

    override fun provide(): ByteArray? {
        if (lastFrame == null) {
            lastFrame = player.provide()
        }

        val data = if (lastFrame != null) lastFrame!!.data else null
        lastFrame = null

        return data
    }

    override fun getChannels(): Int {
        return 2
    }

    override fun getAudioEncodingType(): AudioEncodingType {
        return AudioEncodingType.OPUS
    }

}