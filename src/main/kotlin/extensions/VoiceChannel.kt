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

suspend fun VoiceChannel.join(scope: CoroutineScope): VoiceConnection = join {
    it.setProvider(musicFeature.trackSchedulerFor(guildId).audioProvider)
}.await().also { connection ->
    scope.launch {
        suspend fun memberCountThresholdMet() = voiceStates.asFlow().count() == 1

        launch disconnect@{
            client.flowOf<VoiceStateUpdateEvent>()
                .filter { it.current.userId == client.selfId && it.old.value?.channelId?.value == id }
                .first()
            this@launch.cancel("Bot was disconnected from voice.")
        }

        val noMembersJoinedConstraint = async {
            delay(10_000)
            memberCountThresholdMet()
        }

        val noMembersLeftConstraint = async {
            client.flowOf<VoiceStateUpdateEvent>()
                .filter { it.old.value?.channelId?.value == id && memberCountThresholdMet() }
                .map { true }
                .first()
        }

        if (noMembersJoinedConstraint.await() || noMembersLeftConstraint.await()) {
            connection.disconnect().await()
            cancel("Disconnecting from voice, no members left in channel.")
        }
    }
}
