package xyz.swagbot.commands

import com.mojang.brigadier.builder.*
import discord4j.core.*
import io.facet.discord.commands.*
import io.facet.discord.commands.extensions.*

object PingCommand : ChatCommand(
    name = "Ping",
    aliases = setOf("ping")
) {

    override fun register(client: DiscordClient, node: LiteralArgumentBuilder<ChatCommandSource>) {
        node.executesAsync { context ->
            context.source.message.channel
                .flatMap { it.createMessage("Pong!") }
                .then()
        }
    }
}
