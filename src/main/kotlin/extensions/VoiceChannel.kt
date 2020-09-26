package xyz.swagbot.extensions

import discord4j.core.`object`.entity.channel.*
import discord4j.voice.*
import io.facet.discord.extensions.*
import xyz.swagbot.features.music.*

private val VoiceChannel.musicFeature: Music
    get() = client.feature(Music)

suspend fun VoiceChannel.join(): VoiceConnection = join {
    it.setProvider(musicFeature.trackSchedulerFor(guildId).audioProvider)
}.await()
