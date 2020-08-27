package xyz.swagbot.commands

import com.mojang.brigadier.arguments.StringArgumentType.*
import discord4j.core.*
import discord4j.core.`object`.entity.channel.*
import discord4j.rest.util.*
import io.facet.discord.commands.*
import io.facet.discord.commands.dsl.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*
import kotlinx.coroutines.*
import xyz.swagbot.extensions.*
import xyz.swagbot.features.permissions.*
import xyz.swagbot.util.*

object BringCommand : ChatCommand(
    name = "Bring Members",
    aliases = setOf("bring"),
    scope = Scope.GUILD,
    category = "moderator",
    discordPermsRequired = PermissionSet.of(Permission.MOVE_MEMBERS)
) {

    override fun DSLCommandNode<ChatCommandSource>.register(client: GatewayDiscordClient) {
        require {
            hasBotPermission(PermissionType.MOD)
        }

        runs {

        }

        argument("channel", string()) {
            runs { context ->
                val guild = getGuild()

                val channelToBring: GuildChannel = guild.channels.await()
                    .firstOrNull { it is VoiceChannel && it.name == context.getString("channel") }
                    ?: return@runs getChannel().createEmbed(errorTemplate.andThen {
                        it.setDescription("")
                    }).awaitComplete()

                client.scope.launch {
                    guild.voiceStates.await()
                        .map { it.member.await() }
                        .map { member ->
                            member.edit { it.setNewVoiceChannel(channelToBring.id) }
                        }
                        .forEach { it.async() }
                }
            }
        }
    }
}
