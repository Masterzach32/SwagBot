package xyz.swagbot.commands

import io.facet.commands.*
import io.facet.common.*

object Ping : GlobalApplicationCommand {

    override val request = applicationCommandRequest("ping", "Ping the bot.")

    override suspend fun GlobalSlashCommandContext.execute() {
        event.reply("Pong!").await()
    }
}
