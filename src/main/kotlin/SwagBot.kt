@file:JvmName("SwagBot")

package xyz.swagbot

import discord4j.core.*
import discord4j.core.shard.*
import discord4j.gateway.intent.*
import discord4j.rest.response.*
import io.facet.discord.commands.*
import io.facet.discord.extensions.*
import org.slf4j.*
import xyz.swagbot.commands.*
import xyz.swagbot.features.*
import xyz.swagbot.features.autoroles.*
import xyz.swagbot.features.bgw.*
import xyz.swagbot.features.games.*
import xyz.swagbot.features.guilds.*
import xyz.swagbot.features.music.*
import xyz.swagbot.features.permissions.*
import xyz.swagbot.features.system.*

val logger = LoggerFactory.getLogger(EnvVars.BOT_NAME)

fun main() {
    logger.info("Starting SwagBot (v${EnvVars.CODE_VERSION})")

    val client = DiscordClient.builder(EnvVars.BOT_TOKEN)
        .onClientResponse(ResponseFunction.emptyIfNotFound())
        .build()

    client.gateway()
        .setEnabledIntents(IntentSet.all())
        .setSharding(ShardingStrategy.recommended())
        .withFeatures(GatewayDiscordClient::configure)
        .block()
}

fun GatewayDiscordClient.configure() {
    install(PostgresDatabase) {
        databaseName = EnvVars.POSTGRES_DB
        databaseUsername = EnvVars.POSTGRES_USER
        databasePassword = EnvVars.POSTGRES_PASSWORD
    }

    install(GuildStorage)

    install(ChatCommands) {
        useDefaultHelpCommand = true

        commandPrefix { guildId -> feature(GuildStorage).commandPrefixFor(guildId) }

        registerCommands(
            BringCommand,
            CatCommand,
            ChangePrefixCommand,
            DeleteMessage,
            DisconnectRouletteCommand,
            Dispatcher,
            DogCommand,
            InfoCommand,
            LmgtfyCommand,
            MigrateCommand,
            Ping,
            Prune
        )
    }

    install(Permissions) {
        developers = setOf(97341976214511616, 212311415455744000, 98200921950920704)
    }

    install(Music)

    install(BotPresence)

    install(AutoAssignRole)

    install(ChatGames)

    install(Market)

    install(BestGroupWorldStuff)
}
