package xyz.swagbot.features.bgw

import discord4j.common.util.*
import discord4j.core.*
import discord4j.core.`object`.entity.*
import discord4j.core.event.domain.guild.*
import discord4j.core.event.domain.lifecycle.*
import discord4j.core.event.domain.message.*
import io.facet.core.*
import io.facet.discord.*
import io.facet.discord.event.*
import io.facet.discord.extensions.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class BestGroupWorldStuff private constructor() {

    companion object : DiscordClientFeature<EmptyConfig, BestGroupWorldStuff>("bgw") {

        override fun install(client: GatewayDiscordClient, configuration: EmptyConfig.() -> Unit): BestGroupWorldStuff {
            BotScope.listener<MessageCreateEvent>(client) { event ->
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
                    channel.createMessage("**${event.member.get().mention} I'LL SHOW YOU SIR**").awaitComplete()
                }

                if (channel.id.asLong() == 402224449367179264) {
                    if (message.author.map { it.isBot }.orElse(false))
                        message.delete("Bot commands are not allowed in #chat").await()
                    else if (message.content.startsWith("~raid"))
                        message.delete("Bot commands are not allowed in #chat").await()
                }
            }

            // remove gay role from me
            val gayRoleId = Snowflake.of(584542070002286624)
            val iAmNotWords: Set<String> = setOf("gay", "homo", "rainbow", "gey")
            val delimiter = "[\\W]*".toRegex().pattern
            val regexes: List<Regex> = iAmNotWords
                .map { it.toCharArray().joinToString(separator = delimiter).toRegex() }

            BotScope.listener<MemberUpdateEvent>(client) { event ->
                suspend fun removeGayRole(event: MemberUpdateEvent, roleToRemove: Snowflake) {
                    delay(1000)
                    val newRoles = event.currentRoles.filterNot { it.asLong() == roleToRemove.asLong() }
                    event.member.await().edit { spec ->
                        spec.setRoles(newRoles.toSet())
                    }.await()
                }

                if (event.memberId.asLong() != 97341976214511616)
                    return@listener

                if (event.currentRoles.contains(gayRoleId)) {
                    removeGayRole(event, gayRoleId)
                } else {
                    event.currentRoles.asFlow()
                        .map { client.getRoleById(event.guildId, it).await() }
                        .firstOrNull { role ->
                            role.name.toLowerCase().let { name ->
                                regexes.any { regex ->
                                    //println("$name: ${regex.find(name)} ${name matches regex}")
                                    regex.find(name) != null
                                }
                            }
                        }
                        ?.let { newGayRole -> removeGayRole(event, newGayRole.id) }
                }
            }

            BotScope.listener<ReadyEvent>(client) { event ->
                suspend fun removeGayRole(member: Member, currentRoles: List<Role>, roleToRemove: Snowflake) {
                    delay(1000)
                    val newRoles = currentRoles.filterNot { it.id == roleToRemove }
                        .map { it.id }
                        .toSet()
                    member.edit { spec ->
                        spec.setRoles(newRoles.toSet())
                    }.await()
                }

                val member = client.getMemberById(
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
            BotScope.listener<MemberUpdateEvent>(client) { event ->
                if (event.memberId.asLong() != 97486068630163456)
                    return@listener

                if (!event.currentRoles.contains(gayRoleId)) {
                    delay(5000)
                    val newRoles = event.currentRoles.apply {
                        add(gayRoleId)
                    }
                    event.member.await().edit { spec ->
                        spec.setRoles(newRoles.toSet())
                    }.await()
                }
            }

            return BestGroupWorldStuff()
        }
    }
}
