package xyz.swagbot.commands

import com.mojang.brigadier.*
import io.facet.discord.commands.*
import io.facet.discord.commands.extensions.*

object PingCommand : ChatCommand {

    override fun register(dispatcher: CommandDispatcher<ChatCommandSource>) {
        dispatcher.register(literal("ping").executes { context ->
            context.source.message.channel.flatMap { it.createMessage("Pong!") }.subscribe().let { 1 }
        })
    }
}
