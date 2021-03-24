package xyz.swagbot.features

import discord4j.core.*
import discord4j.core.`object`.entity.*
import discord4j.core.`object`.entity.channel.*
import discord4j.core.event.*
import discord4j.core.event.domain.*
import io.facet.core.*
import io.facet.core.extensions.*
import io.facet.discord.*
import io.facet.discord.commands.*
import io.facet.discord.event.*
import io.facet.discord.extensions.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactive.*
import xyz.swagbot.features.guilds.*

class AmongUs {

    private val sentCodes = mutableSetOf<String>()

    suspend fun announceCode(channel: MessageChannel, host: Member, code: String) {
        channel.sendMessage("${host.displayName} started an Among Us lobby, join with the code **$code**")
    }

    suspend fun setChannelCode(channel: VoiceChannel, code: String) {
        channel.edit {
            it.setName("($code) Innersloth")
        }.awaitComplete()
    }

    suspend fun resetChannelName(channel: VoiceChannel) {
        if (channel.name.startsWith("(")) {
            channel.edit {
                it.setName(channel.name.substring(8 until channel.name.length))
            }.awaitComplete()
        }
    }

    companion object : EventDispatcherFeature<EmptyConfig, AmongUs>(
        keyName = "amongUs",
        requiredFeatures = listOf(GuildStorage, ChatCommands)
    ) {

        override fun EventDispatcher.install(scope: CoroutineScope, configuration: EmptyConfig.() -> Unit): AmongUs {
            return AmongUs().also { feature ->
                scope.listener<PresenceUpdateEvent> { event ->
                    val member = event.member.await()
                    if (event.guildId.asLong() != 97342233241464832 || member.isBot)
                        return@listener

                    val voiceState = member.voiceState.awaitNullable()
                    val voiceChannel = voiceState.channel.awaitNullable()
                    if (voiceChannel.id.asLong() == 765385808151969804) {
                        val amongUsActivity = event.current.activities.firstOrNull { activity ->
                            activity.applicationId.value.asLong() == 477175586805252107
                        }
                        if (amongUsActivity != null) {
                            val joinCode = amongUsActivity.partyId.value
                            val partySize = amongUsActivity.currentPartySize.orElse(0)
                            if (joinCode != null && feature.sentCodes.add(joinCode) && partySize == 1L) {
                                launch { feature.setChannelCode(voiceChannel, joinCode) }
                                feature.announceCode(
                                    event.client.getChannelById(176867794485379072.toSnowflake())
                                        .await() as MessageChannel,
                                    member,
                                    joinCode
                                )
                            }
                        }
                    }
                }

                scope.listener<VoiceStateUpdateEvent> { event ->
                    if (event.current.guildId.asLong() != 97342233241464832)
                        return@listener

                    val voiceChannel = event.old.value.channel.awaitNullable()
                    if (voiceChannel.id.asLong() == 765385808151969804)
                        if (voiceChannel.voiceStates.asFlow().count() == 0)
                            feature.resetChannelName(voiceChannel)
                }
            }
        }
    }
}
