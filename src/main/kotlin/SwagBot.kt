@file:JvmName("SwagBot")

package xyz.swagbot

import discord4j.core.DiscordClient
import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.presence.ClientActivity
import discord4j.core.`object`.presence.ClientPresence
import discord4j.core.event.EventDispatcher
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.shard.ShardingStrategy
import discord4j.gateway.intent.IntentSet
import discord4j.rest.response.ResponseFunction
import io.facet.common.dsl.and
import io.facet.common.listener
import io.facet.common.reply
import io.facet.core.feature
import io.facet.core.features.ApplicationCommands
import io.facet.core.features.ChatCommands
import io.facet.core.install
import io.facet.core.withPlugins
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.swagbot.commands.*
import xyz.swagbot.extensions.commandPrefixFor
import xyz.swagbot.features.AmongUs
import xyz.swagbot.features.BotPresence
import xyz.swagbot.features.Market
import xyz.swagbot.features.autoroles.AutoAssignRole
import xyz.swagbot.features.bgw.BestGroupWorldStuff
import xyz.swagbot.features.games.ChatGames
import xyz.swagbot.features.guilds.GuildStorage
import xyz.swagbot.features.music.Music
import xyz.swagbot.features.permissions.Permissions
import xyz.swagbot.features.system.PostgresDatabase
import xyz.swagbot.util.baseTemplate

val logger: Logger = LoggerFactory.getLogger(EnvVars.BOT_NAME)

fun main() {
    logger.info("Starting SwagBot (v${EnvVars.CODE_VERSION})")

    val client = DiscordClient.builder(EnvVars.BOT_TOKEN)
        .onClientResponse(ResponseFunction.emptyIfNotFound())
        .build()

    client.gateway()
        .setEnabledIntents(IntentSet.all())
        .setSharding(ShardingStrategy.recommended())
        .setInitialPresence { ClientPresence.online(ClientActivity.listening("~help")) }
        .withPlugins(EventDispatcher::configure)
        .withPlugins(GatewayDiscordClient::configure)
        .block()
}

@OptIn(ObsoleteCoroutinesApi::class)
suspend fun EventDispatcher.configure(scope: CoroutineScope) {
    install(scope, PostgresDatabase) {
        databaseName = EnvVars.POSTGRES_DB
        databaseUsername = EnvVars.POSTGRES_USER
        databasePassword = EnvVars.POSTGRES_PASSWORD
    }

    install(scope, GuildStorage)

    install(scope, ChatCommands) {
        useDefaultHelpCommand = true

        commandPrefix { guildId -> feature(GuildStorage).commandPrefixFor(guildId) }

        registerCommands(
            BringCommand,
            CatCommand,
            ChangePrefixCommand,
            Clear,
            Crewlink,
            DogCommand,
            InfoCommand,
            JoinCommand,
            LeaveCommand,
            LeaverClear,
            LmgtfyCommand,
            NowPlayingCommand,
            PauseResumeCommand,
            Premium,
            Queue,
            SkipCommand,
        )
    }

    install(scope, Permissions) {
        developers = setOf(97341976214511616, 212311415455744000, 98200921950920704)
    }

    install(scope, Music)

    install(scope, BotPresence)

    install(scope, AutoAssignRole)

    install(scope, ChatGames)

    install(scope, Market)

    install(scope, BestGroupWorldStuff)

    install(scope, AmongUs)
}

@OptIn(ObsoleteCoroutinesApi::class)
suspend fun GatewayDiscordClient.configure(scope: CoroutineScope) {
    listener<MessageCreateEvent>(scope) { event ->
        if (event.message.userMentionIds.contains(selfId)) {
            val prefix = commandPrefixFor(null)
            event.message.reply(baseTemplate.and {
                description = "Hi **${event.message.author.get().username}**! My command prefix is `$prefix`. To " +
                    "learn more about me, type `${prefix}info`, and to see my commands, type `${prefix}help`."
            })
        }
    }

    install(scope, ApplicationCommands) {
        registerCommand(
            ChangePermissionCommand,
            DisconnectRouletteCommand,
            TTS,
            MigrateCommand,
            Ping,
            Play,
            Prune,
            YouTubeSearch,
            VolumeCommand,
            VoteSkipCommand
        )
    }
}
