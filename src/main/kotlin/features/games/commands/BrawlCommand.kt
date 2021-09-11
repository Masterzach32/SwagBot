package xyz.swagbot.features.games.commands

import io.facet.chatcommands.ChatCommand
import io.facet.chatcommands.ChatCommandSource
import io.facet.chatcommands.DSLCommandNode
import io.facet.chatcommands.runs

object BrawlCommand : ChatCommand(
    name = "Brawl",
    aliases = setOf("brawl", "fight")
) {

    override fun DSLCommandNode<ChatCommandSource>.register() {
        runs {

        }
    }
}
