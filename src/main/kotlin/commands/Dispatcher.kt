package xyz.swagbot.commands

import io.facet.discord.commands.*
import io.facet.discord.commands.dsl.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*
import xyz.swagbot.extensions.*
import xyz.swagbot.features.permissions.*

object Dispatcher : ChatCommand(
    name = "List Dispatcher Nodes",
    aliases = setOf("nodes")
) {

    override fun DSLCommandNode<ChatCommandSource>.register() {
        require { hasBotPermission(PermissionType.DEV) }

        runs {
            val dispatcher = client.feature(ChatCommands).dispatcher

            val text = dispatcher.getAllUsage(dispatcher.root, it.source, false).joinToString("\n")

            getChannel().createMessage(text).awaitComplete()
        }
    }
}