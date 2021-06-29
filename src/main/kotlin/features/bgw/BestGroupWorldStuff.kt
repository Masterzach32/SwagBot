package xyz.swagbot.features.bgw

import discord4j.common.util.*
import discord4j.core.`object`.entity.*
import discord4j.core.event.*
import discord4j.core.event.domain.*
import discord4j.core.event.domain.guild.*
import discord4j.core.event.domain.lifecycle.*
import discord4j.core.event.domain.message.*
import io.facet.core.*
import io.facet.core.extensions.*
import io.facet.discord.*
import io.facet.discord.event.*
import io.facet.discord.extensions.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactive.*
import xyz.swagbot.*
import java.time.*
import java.util.*

class BestGroupWorldStuff private constructor() {

    companion object : EventDispatcherFeature<EmptyConfig, BestGroupWorldStuff>("bgw") {

        override fun EventDispatcher.install(
            scope: CoroutineScope,
            configuration: EmptyConfig.() -> Unit
        ): BestGroupWorldStuff {
            listener<MessageCreateEvent>(scope) { event ->
                val channel = event.message.channel.await()
                val message = event.message

                if (channel.id.asLong() == 402224449367179264) {
                    if (message.attachments.isNotEmpty() || message.embeds.isNotEmpty()) {
                        event.message.delete("No images in #chat.").await()
                    }
                }

                if (message.author.map { !it.isBot }.orElse(false) &&
                    event.guildId.map { it.asLong() == 97342233241464832 }.orElse(false) &&
                    message.content.toLowerCase().contains("sir")) {
                    message.reply("**${event.member.get().mention} I'LL SHOW YOU SIR**")
                }

                if (channel.id.asLong() == 402224449367179264) {
                    if (message.author.map { it.isBot && it.id.asLong() != 219554475055120384 }.orElse(false))
                        message.delete("Bot commands are not allowed in #chat").await()
                    else if (message.content.startsWith("~raid"))
                        message.delete("Bot commands are not allowed in #chat").await()
                }
            }

            listener<MessageCreateEvent>(scope) { event ->
                if (event.guildId.value == 97342233241464832.toSnowflake()) {

                }
            }

            // remove gay role from me
            val gayRoleId = Snowflake.of(584542070002286624)
            val iAmNotWords: Set<String> = setOf("gay", "homo", "rainbow", "gey", "horse", "bottom", "penis")
            val delimiter = "[\\W]*".toRegex().pattern
            val regexes: List<Regex> = iAmNotWords
                .map { it.toCharArray().joinToString(separator = delimiter).toRegex() }

            listener<MemberUpdateEvent>(scope) { event ->
                suspend fun removeGayRole(event: MemberUpdateEvent, roleToRemove: Snowflake) {
                    delay(1000)
                    val newRoles = event.currentRoleIds.filterNot { it.asLong() == roleToRemove.asLong() }
                    event.member.await().edit { spec ->
                        spec.setRoles(newRoles.toSet())
                    }.await()
                }

                if (event.memberId.asLong() !in listOf(97341976214511616, 219554475055120384, 217065780078968833))
                    return@listener

                if (event.currentRoleIds.contains(gayRoleId)) {
                    removeGayRole(event, gayRoleId)
                } else {
                    event.currentRoles.asFlow()
                        .firstOrNull { role ->
                            role.name.lowercase(Locale.getDefault()).let { name ->
                                regexes.any { regex ->
                                    //println("$name: ${regex.find(name)} ${name matches regex}")
                                    regex.find(name) != null
                                }
                            }
                        }
                        ?.let { newGayRole -> removeGayRole(event, newGayRole.id) }
                }
            }

            listener<ReadyEvent>(scope) { event ->
                suspend fun removeGayRole(member: Member, currentRoles: List<Role>, roleToRemove: Snowflake) {
                    delay(1000)
                    val newRoles = currentRoles.filterNot { it.id == roleToRemove }
                        .map { it.id }
                        .toSet()
                    member.edit { spec ->
                        spec.setRoles(newRoles.toSet())
                    }.await()
                }

                val member = event.client.getMemberById(
                    Snowflake.of(97342233241464832),
                    Snowflake.of(97341976214511616)
                ).await()
                val currentRoles = member.roles.await()

                if (currentRoles.any { it.id == gayRoleId }) {
                    removeGayRole(member, currentRoles, gayRoleId)
                } else {
                    currentRoles.asFlow()
                        .firstOrNull { role ->
                            role.name.toLowerCase().let { name ->
                                regexes.any { regex ->
                                    //println("$name: ${regex.find(name)} ${name matches regex}")
                                    regex.find(name) != null
                                }
                            }
                        }
                        ?.let { newGayRole -> removeGayRole(member, currentRoles, newGayRole.id) }
                }
            }

            // add gay role to jack
            listener<MemberUpdateEvent>(scope) { event ->
                if (event.memberId.asLong() != 97486068630163456)
                    return@listener

                if (!event.currentRoleIds.contains(gayRoleId)) {
                    delay(5000)
                    val newRoles = event.currentRoleIds.apply {
                        add(gayRoleId)
                    }
                    event.member.await().edit { spec ->
                        spec.setRoles(newRoles.toSet())
                    }.await()
                }
            }

            // disconnect joevanni and jack after an hour or so
            val ids = setOf(97486068630163456, 212311415455744000)
                .map { it.toSnowflake() }
            listener<VoiceStateUpdateEvent>(scope) { event ->
                val vs = event.current
                if (vs.guildId.asLong() != 97342233241464832 || vs.channelId.value == null)
                    return@listener

                if (event.old.value?.channelId?.value != null)
                    return@listener

                if (!ids.contains(vs.userId))
                    return@listener

                val member = vs.member.await()
                val delay = (2 * 3600 * 1000..6 * 3600 * 1000).random().toLong()
                val kickTime = LocalDateTime.now().plusSeconds(delay / 1000)
                logger.info("${member.tag} will be disconnected at $kickTime")
                launch {
                    val cancellationListener = listener<VoiceStateUpdateEvent>(this) { event ->
                        if (event.current.userId == member.id && event.current.channelId.value == null) {
                            logger.info("${member.tag} left voice early, cancelling disconnect timer.")
                            this@launch.cancel()
                        }
                    }

                    delay(delay)
                    if (member.voiceState.awaitNullable()?.channelId?.unwrap() != null) {
                        cancellationListener.cancel("Completed.")
                        member.edit { it.setNewVoiceChannel(null) }.await()
                        logger.info("Disconnected ${member.tag} for being in voice too long.")
                    } else
                        logger.info("Could not disconnect ${member.tag} because they left voice.")
                }
            }

            listener<MessageCreateEvent>(scope) { event ->
                if (!event.guildId.map { it.asLong() == 97342233241464832 }.orElse(false))
                    return@listener

                event.message.content.let {
                    if ("board" in it && "game" in it && "night" in it) {
                        async { event.message.channel.await().type().await() }
                        delay(5000)
                        event.message.reply("maybe")
                    }
                }
            }

            return BestGroupWorldStuff()
        }
    }
}