@file:JvmName("SwagBot")

package xyz.swagbot

import discord4j.core.*
import discord4j.core.`object`.presence.*
import discord4j.core.event.*
import discord4j.core.event.domain.message.*
import discord4j.core.shard.*
import discord4j.gateway.intent.*
import discord4j.rest.response.*
import io.facet.discord.appcommands.*
import io.facet.discord.commands.*
import io.facet.discord.dsl.*
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
            MigrateCommand,
            Ping,
            Play,
            Prune,
            YouTubeSearch,
            VolumeCommand,
            VoteSkipCommand
        )
    }

    scope.launch {
//        logger.info(
//            restClient.applicationService.getGlobalApplicationCommands(selfId.asLong()).await().toString()
//        )
//        logger.info(
//            restClient.applicationService.getGuildApplicationCommands(selfId.asLong(), 97342233241464832).await().toString()
//        )
    }

    //ApplicationCommandRequ{name=search, description=Search YouTube and select a video to play using reaction buttons., options=[ApplicationCommandOptionData{type=3, name=query, description=The search term to look up on YouTube., required=Possible{true}, choices=null, options=null}, ApplicationCommandOptionData{type=4, name=count, description=The number of results to show., required=Possible{false}, choices=[ApplicationCommandOptionChoiceData{name=Five results, value=5}, ApplicationCommandOptionChoiceData{name=Ten results, value=10}], options=null}], defaultPermission=Possible{true}}
    //ApplicationCommandData{name=search, description=Search YouTube and select a video to play using reaction buttons., options=[ApplicationCommandOptionData{type=3, name=query, description=The search term to look up on YouTube., required=Possible{true}, choices=null, options=null}, ApplicationCommandOptionData{type=4, name=count, description=The number of results to show., required=Possible.absent, choices=[ApplicationCommandOptionChoiceData{name=Five results, value=5}, ApplicationCommandOptionChoiceData{name=Ten results, value=10}], options=null}], defaultPermission=Possible{true}}

}
