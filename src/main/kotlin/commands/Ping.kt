package xyz.swagbot.commands

import io.facet.discord.appcommands.*
import io.facet.discord.extensions.*

object Ping : GlobalApplicationCommand {

    override val request = applicationCommandRequest("ping", "Ping the bot.")

    override suspend fun GlobalInteractionContext.execute() {
        event.reply("Pong!").await()
    }
}
