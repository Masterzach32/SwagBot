package xyz.swagbot.commands

import com.mojang.brigadier.arguments.StringArgumentType.*
import discord4j.core.`object`.entity.*
import discord4j.core.`object`.entity.channel.*
import discord4j.rest.util.*
import io.facet.discord.appcommands.*
import io.facet.discord.appcommands.extensions.*
import io.facet.discord.commands.*
import io.facet.discord.commands.dsl.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactive.*
import reactor.core.publisher.*
import xyz.swagbot.extensions.*
import xyz.swagbot.features.permissions.*

object MigrateCommand : GlobalGuildApplicationCommand, PermissibleApplicationCommand {

    override val request = applicationCommandRequest("migrate", "Move all users from one voice channel to another.") {
        addOption("from", "The voice channel to move users from.", ApplicationCommandOptionType.CHANNEL, true)
        addOption("to", "The voice channel to move users to.", ApplicationCommandOptionType.CHANNEL, true)
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
