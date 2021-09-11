package xyz.swagbot.extensions

import discord4j.core.`object`.entity.channel.VoiceChannel
import discord4j.core.event.domain.VoiceStateUpdateEvent
import discord4j.voice.VoiceConnection
import io.facet.common.await
import io.facet.common.flowOf
import io.facet.common.unwrap
import io.facet.core.feature
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import xyz.swagbot.features.music.Music

private val VoiceChannel.musicFeature: Music
    get() = client.feature(Music)

suspend fun VoiceChannel.joinWithAutoDisconnect(): VoiceConnection = join()
    .withProvider(musicFeature.trackSchedulerFor(guildId).audioProvider)
    .await()
    .also { connection ->
        GlobalScope.launch main@{
            suspend fun memberCountThresholdMet() = voiceStates.asFlow().count() == 1

            launch {
                delay(10_000)
                if (memberCountThresholdMet()) {
                    connection.disconnect().await()
                    musicFeature.updateLastConnectedChannelFor(guildId, null)
                    this@main.cancel("Disconnecting from voice, no members left in channel.")
                }
            }

            client.flowOf<VoiceStateUpdateEvent>()
                .filter { event -> event.isLeaveEvent && event.old.flatMap { it.channelId }.unwrap() == id }
                .collect { event ->
                    when {
                        memberCountThresholdMet() -> {
                            connection.disconnect().await()
                            musicFeature.updateLastConnectedChannelFor(guildId, null)
                            cancel("Disconnecting from voice, no members left in channel.")
                        }
                        event.current.userId == client.selfId -> {
                            musicFeature.updateLastConnectedChannelFor(guildId, null)
                            cancel("Bot was disconnected from voice.")
                        }
                    }
                }
        }
    }
