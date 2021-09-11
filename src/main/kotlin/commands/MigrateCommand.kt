package xyz.swagbot.commands

import com.mojang.brigadier.arguments.StringArgumentType.string
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.User
import discord4j.core.`object`.entity.channel.Channel
import discord4j.core.`object`.entity.channel.VoiceChannel
import discord4j.rest.util.Permission
import io.facet.chatcommands.ChatCommandSource
import io.facet.chatcommands.DSLCommandNode
import io.facet.chatcommands.getString
import io.facet.chatcommands.runs
import io.facet.commands.*
import io.facet.common.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import reactor.core.publisher.Mono
import xyz.swagbot.extensions.hasBotPermission
import xyz.swagbot.features.permissions.PermissionType

object MigrateCommand : GlobalGuildApplicationCommand, PermissibleApplicationCommand {

    override val request = applicationCommandRequest("migrate", "Move all users from one voice channel to another.") {
        channel("from", "The voice channel to move users from.", true)
        channel("to", "The voice channel to move users to.", true)
    }

    override suspend fun hasPermission(user: User, guild: Guild?): Boolean {
        return user.asMember(guild!!.id).await().basePermissions.await().contains(Permission.ADMINISTRATOR)
    }

    override suspend fun GuildSlashCommandContext.execute() {
        val from: Mono<Channel> by options
        val fromChannel = from.awaitNullable()
        if (fromChannel !is VoiceChannel)
            return event.reply("Channel **${fromChannel?.mention}** specified as `from` is not a voice channel.")
                .withEphemeral(true)
                .await()

        val to: Mono<Channel> by options
        val toChannel = to.awaitNullable()
        if (toChannel !is VoiceChannel)
            return event.reply("Channel **${toChannel?.mention}** specified as `to` is not a voice channel.")
                .withEphemeral(true)
                .await()

        acknowledge()

        val numMoved = coroutineScope {
            fromChannel.voiceStates.asFlow()
                .map { it.member.await() }
                .toList()
                .map { member -> member.edit().withNewVoiceChannelOrNull(toChannel.id) }
                .onEach { launch { it.await() } }
                .count()
        }

        event.interactionResponse.sendFollowupMessage {
            content = "Moved **${numMoved}** members from **${fromChannel.mention}** to **${toChannel.mention}**"
        }
    }

    fun DSLCommandNode<ChatCommandSource>.register() {
        require {
            hasBotPermission(PermissionType.MOD)
        }

        argument("fromChannel", string()) {
            argument("toChannel", string()) {
                runs { context ->
                    val guild = getGuild()
                    val fromChannelName = context.getString("fromChannel")
                    val fromChannel = guild.getVoiceChannelByName(fromChannelName)
                    val toChannelName = context.getString("toChannel")
                    val toChannel = guild.getVoiceChannelByName(toChannelName)

                    if (fromChannel == null) {
                        message.reply("I could not find a voice channel with the name **${fromChannelName}**")
                        return@runs
                    }
                    if (toChannel == null) {
                        message.reply("I could not find a voice channel with the name **${toChannelName}**")
                        return@runs
                    }

                    launch { getChannel().type().await() }

                    val numMoved = fromChannel.voiceStates.asFlow()
                        .map { it.member.await() }
                        .toList()
                        .map { member -> member.edit().withNewVoiceChannelOrNull(toChannel.id) }
                        .map { launch { it.await() } }
                        .count()

                    message.reply("Moved **${numMoved}** members from **${fromChannelName}** to **${toChannelName}**")
                }
            }
        }
    }
}
