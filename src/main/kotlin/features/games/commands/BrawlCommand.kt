package xyz.swagbot.features.games.commands

import io.facet.chatcommands.*

object BrawlCommand : ChatCommand(
    name = "Brawl",
    aliases = setOf("brawl", "fight")
) {

    override fun DSLCommandNode<ChatCommandSource>.register() {
        runs {

        }
    }
}
