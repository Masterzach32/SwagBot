package xyz.swagbot.commands

import io.facet.discord.commands.*
import io.facet.discord.commands.dsl.*
import io.facet.discord.commands.extensions.*

object Ping : ChatCommand(
    name = "Ping",
    aliases = setOf("ping")
) {

    override fun DSLCommandNode<ChatCommandSource>.register() {
        runs {
            respondMessage("Pong!")
        }
    }
}
