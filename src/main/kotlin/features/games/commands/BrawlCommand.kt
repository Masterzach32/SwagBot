package xyz.swagbot.features.games.commands

import io.facet.discord.commands.*
import io.facet.discord.commands.dsl.*
import io.facet.discord.commands.extensions.*

object BrawlCommand : ChatCommand(
    name = "Brawl",
    aliases = setOf("brawl", "fight")
) {

    override fun DSLCommandNode<ChatCommandSource>.register() {
        runs {

        }
    }
}