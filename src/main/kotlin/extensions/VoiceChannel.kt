package xyz.swagbot.extensions

import discord4j.core.`object`.entity.channel.*
import io.facet.discord.extensions.*
import xyz.swagbot.features.music.*

private val VoiceChannel.musicFeature: Music
    get() = client.feature(Music)

suspend fun VoiceChannel.joinVoice() {
    musicFeature.voiceConnections[guildId] = join {
        it.setProvider(musicFeature.trackSchedulerFor(guildId).audioProvider)
    }.await()
}
