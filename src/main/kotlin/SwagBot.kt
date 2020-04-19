@file:JvmName("SwagBot")

package xyz.swagbot

import discord4j.core.*
import io.facet.discord.commands.*
import io.facet.discord.extensions.*
import org.slf4j.*
import xyz.swagbot.commands.*
import xyz.swagbot.features.*
import xyz.swagbot.features.autoroles.*
import xyz.swagbot.features.bgw.*
import xyz.swagbot.features.guilds.*
import xyz.swagbot.features.music.*
import xyz.swagbot.features.permissions.*

val logger = LoggerFactory.getLogger("SwagBot")

fun main() {
    logger.info("Starting SwagBot...")

    logger.info("Constructing DiscordClient instance.")
    DiscordClientBuilder.create(System.getenv("BOT_TOKEN")).build().apply {
        logger.info("Installing bot features into DiscordClient instance.")
        install(SystemInteraction)

        install(GuildStorage)

        install(ChatCommands) {
            commandPrefix { guildId -> feature(GuildStorage).commandPrefixFor(guildId) }

            registerCommands(
                BringCommand,
                ChangePrefixCommand,
                DisconnectRouletteCommand,
                HelpCommand,
                InfoCommand,
                MigrateCommand,
                PingCommand,
                PruneCommand
            )
        }

        install(Permissions) {
            developers = listOf(97341976214511616L, 212311415455744000L, 98200921950920704L)
        }

        install(Music)

        install(BotPresence)

        install(AutoAssignRole)

        install(Market)

        install(BestGroupWorldStuff)

        logger.info("Done configuring client, logging into Discord.")
        login().block()
    }
}