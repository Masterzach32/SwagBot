package xyz.swagbot.commands

import com.mojang.brigadier.*
import io.facet.discord.commands.*
import io.facet.discord.commands.extensions.*

object HelpCommand : ChatCommand {

    override fun register(dispatcher: CommandDispatcher<ChatCommandSource>) {
        listOf("help", "h", "commands").forEach { alias ->
            dispatcher.register(literal(alias).executes { context ->
                val event = context.source
                event.message.channel
                    .flatMap { channel ->
                        channel.createMessage(
                            dispatcher.getAllUsage(dispatcher.root, event, false).reduce { acc, s -> "$acc\n$s" }
                        )
                    }
                    .subscribe()
                    .let { 1 }
            })
        }
    }
}
