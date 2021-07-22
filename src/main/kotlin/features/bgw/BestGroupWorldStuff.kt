package xyz.swagbot.features.bgw

import discord4j.common.util.*
import discord4j.core.`object`.audit.*
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
import io.facet.discord.exposed.*
import io.facet.discord.extensions.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactive.*
import org.jetbrains.exposed.sql.*
import xyz.swagbot.*
import java.time.*
import java.util.*

class BestGroupWorldStuff private constructor() {

    companion object : EventDispatcherFeature<EmptyConfig, BestGroupWorldStuff>("bgw") {

        override suspend fun EventDispatcher.install(
            scope: CoroutineScope,
            configuration: EmptyConfig.() -> Unit
        ): BestGroupWorldStuff {
            sql { create(ListTable) }

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
                    message.content.lowercase().contains("sir")) {
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
                if (event.guildId.unwrap() == 97342233241464832.toSnowflake()) {

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
                    event.member.await().edit()
                        .withRoles(newRoles.toSet())
                        .await()
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
                    member.edit()
                        .withRoles(newRoles.toSet())
                        .await()
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
                            role.name.lowercase().let { name ->
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
                    event.member.await().edit()
                        .withRoles(newRoles.toSet())
                        .await()
                }
            }

            // automatically disconnect people on "the list" after an hour or so
            val ids = setOf(97486068630163456, 140584266315726848)
                .map { it.toSnowflake() }

            flowOf<VoiceStateUpdateEvent>()
                .filter { it.isLeaveEvent && it.old.get().guildId == 97342233241464832.toSnowflake() }
                .onEach { delay(5000) }
                .mapNotNull { event ->
                    delay(5000)
                    val old = event.old.get()
                    val log = event.current.guild.await().auditLog
                        .withActionType(ActionType.MEMBER_DISCONNECT)
                        .withLimit(5)
                        .await()
                        .reduce { acc, part -> acc.combine(part) }

                    log.entries.firstOrNull { it.targetId.unwrap() == old.userId }
                }
                .onEach { entry ->
                    ListTable.insert {
                        it[user_id] = entry.responsibleUserId.get()
                    }
                }

            listener<VoiceStateUpdateEvent>(scope) { event ->
                val vs = event.current
                if (vs.guildId.asLong() != 97342233241464832 || vs.channelId.unwrap() == null)
                    return@listener

                if (event.old.unwrap()?.channelId?.unwrap() != null)
                    return@listener

                if (!ids.contains(vs.userId))
                    return@listener

                val member = vs.member.await()
                val delay = (1 * 3600 * 1000..6 * 3600 * 1000).random().toLong()
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
                        member.edit().withNewVoiceChannelOrNull(null).await()
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
