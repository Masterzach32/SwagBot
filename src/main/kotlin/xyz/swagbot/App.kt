package xyz.swagbot

import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import net.masterzach32.commands4k.CommandListener
import net.masterzach32.commands4k.Permission
import org.slf4j.LoggerFactory
import sx.blah.discord.api.ClientBuilder
import xyz.swagbot.commands.*
import xyz.swagbot.commands.admin.*
import xyz.swagbot.commands.mod.*
import xyz.swagbot.commands.music.*
import xyz.swagbot.commands.normal.*
import xyz.swagbot.database.*
import xyz.swagbot.events.*
import xyz.swagbot.plugins.PluginStore

/*
 * SwagBot - Created on 8/22/17
 * Author: Zach Kozar
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at the project root.
 */

/**
 * @author Zach Kozar
 * @version 8/22/17
 */
const val VERSION = "2.0.1.107"
const val DEFAULT_COMMAND_PREFIX = "~"

val logger = LoggerFactory.getLogger("SwagBot Manager")!!

val audioPlayerManager = DefaultAudioPlayerManager()

lateinit var cmds: CommandListener

fun main(args: Array<String>) {
    logger.info("Starting SwagBot version $VERSION.")

    getDatabaseConnection(args)

    logger.info("Starting Lavaplayer audio engine.")
    AudioSourceManagers.registerRemoteSources(audioPlayerManager)

    logger.info("Creating discord client object.")
    val client = ClientBuilder().withToken(getKey("discord_bot_token")).withRecommendedShardCount().build()

    logger.info("Registering event listeners.")
    client.dispatcher.registerListener(CommandExecutedHandler)
    client.dispatcher.registerListener(GuildCreateHandler)
    client.dispatcher.registerListener(GuildLeaveHandler)
    client.dispatcher.registerListener(ReadyHandler)
    client.dispatcher.registerListener(MessageHandler)
    client.dispatcher.registerListener(NewUserHandler)
    client.dispatcher.registerListener(UserJoinEvent)
    client.dispatcher.registerListener(UserLeaveEvent)
    client.dispatcher.registerListener(UserMovedEvent)
    client.dispatcher.registerListener(RoleHandler)
    client.dispatcher.registerListener(ShardDisconnectHandler)
    client.dispatcher.registerListener(UserStatusListener)

    logger.info("Initializing commands.")
    cmds = CommandListener(
            client.dispatcher,
            { it?.getCommandPrefix() ?: DEFAULT_COMMAND_PREFIX },
            {
                if (it == null)
                    this.getBotDMPermission()
                else {
                    val perm = this.getBotPermission(it)
                    if (it.owner == this && perm != Permission.DEVELOPER)
                        Permission.ADMIN
                    else
                        perm
                }
            }
    )

    // basic
    cmds.add(DonateCommand, InfoCommand, InviteCommand, PingCommand, SupportCommand, StatsCommand)
    // music
    cmds.add(AutoPlayCommand, RefreshAudioPlayerCommand, QueueCommand2, NowPlayingCommand2)
    cmds.add(ClearCommand)
    cmds.add(LeaverClearCommand)
    cmds.add(LoopCommand)
    cmds.add(MoveTrackCommand)
    cmds.add(NowPlayingCommand)
    cmds.add(PauseResumeCommand)
    cmds.add(PlayCommand)
    cmds.add(QueueCommand)
    cmds.add(RemoveDuplicatesCommand)
    cmds.add(RemoveTrackCommand)
    cmds.add(ReplayCommand)
    cmds.add(SearchCommand)
    cmds.add(SeekCommand)
    cmds.add(ShuffleCommand)
    cmds.add(SkipCommand)
    cmds.add(SkipToCommand)
    cmds.add(VoteSkipCommand)
    // normal
    cmds.add(LookupCRNCommand, PollCommand)
    cmds.add(CatCommand)
    cmds.add(DogCommand)
    cmds.add(BrawlCommand)
    cmds.add(IAmCommand)
    cmds.add(IAmNotCommand)
    cmds.add(JoinCommand)
    cmds.add(LmgtfyCommand)
    cmds.add(MassAfkCommand)
    cmds.add(R8BallCommand)
    cmds.add(RockPaperScissorsCommand)
    cmds.add(StrawpollCommand)
    cmds.add(UrbanDictionaryCommand)
    cmds.add(UrlShortenCommand)
    cmds.add(VoiceCommand)
    cmds.add(VolumeCommand)
    // mod
    cmds.add(BringCommand)
    cmds.add(MigrateCommand)
    cmds.add(PruneCommand)
    // admin
    cmds.add(AutoAssignRoleCommand)
    cmds.add(ChangePrefixCommand)
    //cmds.add(ChatOnlyCommand)
    cmds.add(EditPermissionsCommand)
    cmds.add(GameSwitchCommand)
    // dev
    cmds.add(
            ShutdownCommand,
            GarbageCollectionCommand,
            JvmStatsCommand,
            SetMotdCommand,
            ShardStatusCommand
    )

    cmds.sortCommands()

    PluginStore.loadAllPlugins(cmds)

    client.login()
    logger.info("Waiting to receive guilds...")
}