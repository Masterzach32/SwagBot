package xyz.swagbot.commands

import io.facet.commands.GlobalApplicationCommand
import io.facet.commands.GlobalSlashCommandContext
import io.facet.commands.applicationCommandRequest
import io.facet.common.await

object Ping : GlobalApplicationCommand {

    override val request = applicationCommandRequest("ping", "Ping the bot.")

    override suspend fun GlobalSlashCommandContext.execute() {
        event.reply("Pong!").await()
    }
}
