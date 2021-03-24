@file:JvmName("SwagBot")

package xyz.swagbot

import discord4j.core.*
import discord4j.core.`object`.presence.*
import discord4j.core.event.*
import discord4j.core.event.domain.message.*
import discord4j.core.shard.*
import discord4j.gateway.intent.*
import discord4j.rest.response.*
import io.facet.discord.commands.*
import io.facet.discord.event.*
import io.facet.discord.extensions.*
import kotlinx.coroutines.*
import org.slf4j.*
import xyz.swagbot.commands.*
import xyz.swagbot.extensions.*
import xyz.swagbot.features.*
import xyz.swagbot.features.autoroles.*
import xyz.swagbot.features.bgw.*
import xyz.swagbot.features.games.*
import xyz.swagbot.features.guilds.*
import xyz.swagbot.features.music.*
import xyz.swagbot.features.permissions.*
import xyz.swagbot.features.system.*
import xyz.swagbot.util.*

val logger: Logger = LoggerFactory.getLogger(EnvVars.BOT_NAME)

fun main() {
    logger.info("Starting SwagBot (v${EnvVars.CODE_VERSION})")

    val client = DiscordClient.builder(EnvVars.BOT_TOKEN)
        .onClientResponse(ResponseFunction.emptyIfNotFound())
        .build()

    client.gateway()
        .setEnabledIntents(IntentSet.all())
        .setSharding(ShardingStrategy.recommended())
        .setInitialStatus { Presence.online(Activity.listening("~help")) }
        .withFeatures(EventDispatcher::configure)
        .withFeatures(GatewayDiscordClient::configure)
        .block()
}

fun EventDispatcher.configure(scope: CoroutineScope) {
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
            ChangePermissionCommand,
            ChangePrefixCommand,
            Clear,
            Crewlink,
            DisconnectRouletteCommand,
            DogCommand,
            InfoCommand,
            JoinCommand,
            LeaveCommand,
            LeaverClear,
            LmgtfyCommand,
            MigrateCommand,
            NowPlayingCommand,
            PauseResumeCommand,
            Ping,
            Play,
            Premium,
            Prune,
            Queue,
            SkipCommand,
            VolumeCommand,
            VoteSkipCommand,
            YouTubeSearch
        )
    }

    install(Music)

    install(BotPresence)

    install(AutoAssignRole)

    install(ChatGames)

    install(Market)

    install(BestGroupWorldStuff)

    install(AmongUs)
}

fun GatewayDiscordClient.configure(scope: CoroutineScope) {
    listener<MessageCreateEvent>(scope) { event ->
        if (event.message.userMentionIds.contains(selfId)) {
            val prefix = commandPrefixFor(null)
            event.message.reply(baseTemplate.andThen {
                description = "Hi **${event.message.author.get().username}**! My command prefix is `$prefix`. To " +
                    "learn more about me, type `${prefix}info`, and to see my commands, type `${prefix}help`."
            })
        }
    }

    install(Permissions) {
        developers = setOf(97341976214511616, 212311415455744000, 98200921950920704)
    }
}
