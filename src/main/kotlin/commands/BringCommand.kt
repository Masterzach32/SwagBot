package xyz.swagbot.commands

import com.mojang.brigadier.*
import com.mojang.brigadier.arguments.StringArgumentType.*
import io.facet.discord.commands.*
import io.facet.discord.commands.extensions.*
import xyz.swagbot.features.permissions.*

object BringCommand : ChatCommand {

    override fun register(dispatcher: CommandDispatcher<ChatCommandSource>) {
        listOf("bring").map { alias ->
            literal(alias).requires {
                it.hasBotPermission(PermissionType.MOD)
            }.then(argument("channel", string()).executes { context ->
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
                        }.collectList()
                }.subscribe().let { 1 }
            }).executes { context ->
                1
            }
        }
    }
}
