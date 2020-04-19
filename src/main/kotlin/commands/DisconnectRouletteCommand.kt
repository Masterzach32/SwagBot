package xyz.swagbot.commands

import com.mojang.brigadier.*
import io.facet.discord.commands.*
import io.facet.discord.commands.extensions.*

object DisconnectRouletteCommand : ChatCommand {

    override fun register(dispatcher: CommandDispatcher<ChatCommandSource>) {
        listOf("disconnectroulette", "droulette").map { alias ->
            literal(alias).executes { context ->
                context.source.member.ifPresent { member ->
                    member.voiceState.flatMap { memberVs ->
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
                    }.subscribe()
                }.let { 1 }
            }
        }.forEach { dispatcher.register(it) }
    }
}
