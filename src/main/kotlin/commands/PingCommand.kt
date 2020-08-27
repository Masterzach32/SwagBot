package xyz.swagbot.commands

import discord4j.core.*
import io.facet.discord.commands.*
import io.facet.discord.commands.dsl.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*

object PingCommand : ChatCommand(
    name = "Ping",
    aliases = setOf("ping")
) {

    override fun DSLCommandNode<ChatCommandSource>.register(client: GatewayDiscordClient) {
        runs {
            getChannel().createMessage("Pong!").awaitComplete()
        }
    }
}
