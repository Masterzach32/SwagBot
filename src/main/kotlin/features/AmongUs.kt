package xyz.swagbot.features

import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.channel.MessageChannel
import discord4j.core.`object`.entity.channel.VoiceChannel
import discord4j.core.event.EventDispatcher
import discord4j.core.event.domain.PresenceUpdateEvent
import discord4j.core.event.domain.VoiceStateUpdateEvent
import io.facet.common.*
import io.facet.core.EmptyConfig
import io.facet.core.EventDispatcherFeature
import io.facet.core.features.ChatCommands
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import xyz.swagbot.features.guilds.GuildStorage

class AmongUs {

    private val sentCodes = mutableSetOf<String>()

    suspend fun announceCode(channel: MessageChannel, host: Member, code: String) {
        channel.sendMessage("${host.displayName} started an Among Us lobby, join with the code **$code**")
    }

    suspend fun setChannelCode(channel: VoiceChannel, code: String) {
        channel.edit()
            .withName("($code) Innersloth")
            .awaitComplete()
    }

    suspend fun resetChannelName(channel: VoiceChannel) {
        if (channel.name.startsWith("(")) {
            channel.edit()
                .withName(channel.name.substring(8 until channel.name.length))
                .awaitComplete()
        }
    }

    companion object : EventDispatcherFeature<EmptyConfig, AmongUs>(
        keyName = "amongUs",
        requiredFeatures = listOf(GuildStorage, ChatCommands)
    ) {

        override suspend fun EventDispatcher.install(
            scope: CoroutineScope,
            configuration: EmptyConfig.() -> Unit
        ): AmongUs {
            return AmongUs().also { feature ->
                listener<PresenceUpdateEvent>(scope) { event ->
                    val member = event.member.await()
                    if (event.guildId.asLong() != 97342233241464832 || member.isBot)
                        return@listener

                    val voiceState = member.voiceState.awaitNullable()
                    val voiceChannel = voiceState?.channel?.awaitNullable()
                    if (voiceChannel?.id?.asLong() == 765385808151969804) {
                        val amongUsActivity = event.current.activities.firstOrNull { activity ->
                            activity.applicationId.unwrap()?.asLong() == 477175586805252107
                        }
                        if (amongUsActivity != null) {
                            val joinCode = amongUsActivity.partyId.unwrap()
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

                listener<VoiceStateUpdateEvent>(scope) { event ->
                    if (event.current.guildId.asLong() != 97342233241464832)
                        return@listener

                    val voiceChannel = event.old.unwrap()?.channel?.awaitNullable()
                    if (voiceChannel?.id?.asLong() == 765385808151969804)
                        if (voiceChannel.voiceStates.asFlow().count() == 0)
                            feature.resetChannelName(voiceChannel)
                }
            }
        }
    }
}
