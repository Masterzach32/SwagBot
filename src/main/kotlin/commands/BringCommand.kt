package xyz.swagbot.commands

import com.mojang.brigadier.arguments.StringArgumentType.*
import com.mojang.brigadier.builder.*
import discord4j.core.*
import io.facet.discord.commands.*
import io.facet.discord.commands.extensions.*
import xyz.swagbot.extensions.*
import xyz.swagbot.features.permissions.*

object BringCommand : ChatCommand(
    name = "Bring Members",
    aliases = setOf("bring"),
    scope = Scope.GUILD
) {

    override fun register(client: DiscordClient, node: LiteralArgumentBuilder<ChatCommandSource>) {
        node.requires {
            it.hasBotPermission(PermissionType.MOD)
        }.then(argument("channel", string()).executesAsync { context ->
            context.source.guild.flatMap { guild ->
                guild.channels
                    .filter { it.name == context.getString("channel") }
                    .flatMap { channel ->
                        guild.voiceStates.flatMap { vs ->
                            vs.member.flatMap { member ->
                                member.edit {
                                    it.setNewVoiceChannel(channel.id)
                                }
                            }
                        }
                    }
                    .then()
            }
        }).executes { context ->
            1
        }
    }
}
