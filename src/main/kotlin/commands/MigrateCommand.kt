package xyz.swagbot.commands

import com.mojang.brigadier.arguments.StringArgumentType.*
import com.mojang.brigadier.builder.*
import discord4j.core.*
import discord4j.core.`object`.entity.*
import discord4j.core.`object`.util.*
import io.facet.discord.commands.*
import io.facet.discord.commands.extensions.*
import reactor.core.publisher.*
import xyz.swagbot.extensions.*
import xyz.swagbot.features.permissions.*

object MigrateCommand : ChatCommand(
    name = "Migrate Members",
    aliases = setOf("migrate", "m"),
    scope = Scope.GUILD,
    discordPermsRequired = PermissionSet.of(Permission.MOVE_MEMBERS)
) {

    override fun register(client: DiscordClient, node: LiteralArgumentBuilder<ChatCommandSource>) {
        node.requires {
            it.hasBotPermission(PermissionType.MOD)
        }.then(argument("fromChannel", string()).then(argument("toChannel", string()).executesAsync { context ->
            context.source.guild.flatMap { guild ->
                guild.getVoiceChannelByName(context.getString("fromChannel")).flatMap { fromChannel ->
                    guild.getVoiceChannelByName(context.getString("toChannel")).flatMap { toChannel ->
                        fromChannel.voiceStates.flatMap { vs ->
                            vs.member.flatMap { member ->
                                member.edit {
                                    it.setNewVoiceChannel(toChannel.id)
                                }
                            }
                        }.then()
                    }
                }
            }
        })).executes { context ->
            1
        }
    }
}
