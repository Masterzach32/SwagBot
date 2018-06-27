package xyz.swagbot

import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.typesafe.config.ConfigFactory
import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Command
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
import sx.blah.discord.util.EmbedBuilder
import xyz.swagbot.utils.*
import java.io.File
import java.lang.management.MemoryType
import java.lang.management.ManagementFactory
import java.lang.management.MemoryNotificationInfo
import javax.management.NotificationListener
import javax.management.NotificationEmitter
import javax.script.ScriptEngineManager

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

val config = ConfigFactory.load()!!
val logger = LoggerFactory.getLogger("SwagBot Manager")!!

val audioPlayerManager = DefaultAudioPlayerManager()

lateinit var cmds: CommandListener

fun main(args: Array<String>) {
    logger.info("Starting SwagBot version ${config.getString("bot.build")}.")

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
            { it?.getCommandPrefix() ?: config.getString("defaults.command_prefix") },
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
    client.dispatcher.registerListener(cmds)

    // basic
    cmds.add(DonateCommand, InfoCommand, InviteCommand, PingCommand, SupportCommand)
    // music
    cmds.add(AutoPlayCommand, RefreshAudioPlayerCommand)
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
            StatsCommand,
            ShardStatusCommand
    )

    cmds.sortCommands()

    client.login()
    logger.info("Waiting to receive guilds...")

    // SwagBot threads
    //DailyUpdate.init(client)

    // heuristic to find the tenured pool (largest heap) as seen on http://www.javaspecialists.eu/archive/Issue092.html
    val tenuredGenPool = ManagementFactory.getMemoryPoolMXBeans()
            .first { it.type == MemoryType.HEAP && it.isUsageThresholdSupported }
    // we do something when we reached 85% of memory usage
    tenuredGenPool.collectionUsageThreshold = Math.floor(tenuredGenPool.usage.max * 0.85).toLong()

    // set a listener
    val emitter = ManagementFactory.getMemoryMXBean() as NotificationEmitter
    emitter.addNotificationListener(NotificationListener { n, _ ->
        if (n.type == MemoryNotificationInfo.MEMORY_COLLECTION_THRESHOLD_EXCEEDED) {
            val maxMemory = tenuredGenPool.usage.max.toDouble()
            val usedMemory = tenuredGenPool.usage.used.toDouble()

            AdvancedMessageBuilder(client.applicationOwner.orCreatePMChannel).withEmbed(
                    EmbedBuilder().withColor(RED)
                            .withTitle("Memory usage running high (${(usedMemory/maxMemory*100).toInt()}%), restarting!")
                            .appendField("Max Memory", "${maxMemory / Math.pow(2.0, 20.0)} MB", true)
                            .appendField("Used Memory", "${usedMemory / Math.pow(2.0, 20.0)} MB", true)
            ).build()

            //shutdown(client, ExitCode.OUT_OF_MEMORY)
        }
    }, null, null)
}