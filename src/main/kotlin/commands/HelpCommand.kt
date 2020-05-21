package xyz.swagbot.commands

import com.mojang.brigadier.builder.*
import discord4j.core.*
import io.facet.discord.commands.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*

object HelpCommand : ChatCommand(
    name = "Help",
    aliases = setOf("help", "h"),
    scope = Scope.GUILD
) {

    override fun register(client: DiscordClient, node: LiteralArgumentBuilder<ChatCommandSource>) {
        node.executesAsync { context ->
            val event = context.source
            val dispatcher = context.source.client.feature(ChatCommands).dispatcher
            event.message.channel.flatMap { channel ->
                channel.createMessage(
                    dispatcher.getAllUsage(dispatcher.root, event, false).reduce { acc, s -> "$acc\n$s" }
                )
            }.then()
        }
    }
}
