package xyz.swagbot.extensions

import discord4j.core.`object`.entity.channel.*
import discord4j.core.event.domain.*
import discord4j.voice.*
import io.facet.core.extensions.*
import io.facet.discord.extensions.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactive.*
import xyz.swagbot.features.music.*

private val VoiceChannel.musicFeature: Music
    get() = client.feature(Music)

suspend fun VoiceChannel.join(): VoiceConnection = join {
    it.setProvider(musicFeature.trackSchedulerFor(guildId).audioProvider)
}.await().also { connection ->
    GlobalScope.launch main@{
        suspend fun memberCountThresholdMet() = voiceStates.asFlow().count() == 1

        launch {
            delay(10_000)
            if (memberCountThresholdMet()) {
                connection.disconnect().await()
                this@main.cancel("Disconnecting from voice, no members left in channel.")
            }
        }

        client.flowOf<VoiceStateUpdateEvent>()
            .filter { event -> event.isLeaveEvent && event.old.flatMap { it.channelId }.unwrap() == id }
            .collect { event ->
                when {
                    memberCountThresholdMet() -> {
                        connection.disconnect().await()
                        cancel("Disconnecting from voice, no members left in channel.")
                    }
                    event.current.userId == client.selfId -> cancel("Bot was disconnected from voice.")
                }
            }
    }
}
