package xyz.swagbot.commands

import com.mojang.brigadier.builder.*
import discord4j.core.*
import io.facet.discord.commands.*
import io.facet.discord.commands.extensions.*

object DisconnectRouletteCommand : ChatCommand(
    name = "Disconnect Roulette",
    aliases = setOf("droulette"),
    scope = Scope.GUILD
) {

    override fun register(client: DiscordClient, node: LiteralArgumentBuilder<ChatCommandSource>) {
        node.executesAsync { context ->
            val source = context.source
            source.member.get().voiceState.flatMap { memberVs ->
                memberVs.channel.flatMap { channel ->
                    channel.voiceStates
                        .flatMap { it.member }
                        .collectList()
                        .flatMap { members ->
                            members[(0 until members.size).random()].edit {
                                it.setNewVoiceChannel(null)
                            }
                        }
                }
            }
        }
    }
}
