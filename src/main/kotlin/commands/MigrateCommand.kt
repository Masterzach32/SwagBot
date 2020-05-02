package xyz.swagbot.commands

import com.mojang.brigadier.*
import com.mojang.brigadier.arguments.StringArgumentType.*
import discord4j.core.`object`.entity.*
import io.facet.discord.commands.*
import io.facet.discord.commands.extensions.*
import reactor.core.publisher.*
import xyz.swagbot.extensions.*
import xyz.swagbot.features.permissions.*

object MigrateCommand : ChatCommand {

    override fun register(dispatcher: CommandDispatcher<ChatCommandSource>) {
        listOf("migrate", "m").map { alias ->
            literal(alias).requires {
                it.hasBotPermission(PermissionType.MOD)
            }.then(argument("fromChannel", string()).then(argument("toChannel", string()).executes { context ->
                context.source.guild.flatMap { guild ->
                    guild.channels
                        .filter { it.name == context.getString("fromChannel") }
                        .toMono()
                        .cast<VoiceChannel>()
                        .flatMap { fromChannel ->
                            guild.channels
                                .filter { it.name == context.getString("toChannel") }
                                .toMono()
                                .cast<VoiceChannel>()
                                .flatMap { toChannel ->
                                    fromChannel.voiceStates.flatMap { vs ->
                                        vs.member.flatMap { member ->
                                            member.edit {
                                                it.setNewVoiceChannel(toChannel.id)
                                            }
                                        }
                                    }.collectList()
                                }
                        }
                }.subscribe().let { 1 }
            })).executes { context ->
                1
            }
        }.forEach { dispatcher.register(it) }
    }
}
