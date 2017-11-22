package xyz.swagbot

import com.typesafe.config.ConfigFactory
import net.masterzach32.commands4k.CommandListener
import net.masterzach32.commands4k.CommandManager
import net.masterzach32.commands4k.Permission
import org.slf4j.LoggerFactory
import sx.blah.discord.api.ClientBuilder
import xyz.swagbot.commands.*
import xyz.swagbot.commands.admin.*
import xyz.swagbot.commands.dev.ShutdownCommand
import xyz.swagbot.commands.mod.*
import xyz.swagbot.commands.normal.*
import xyz.swagbot.database.*
import xyz.swagbot.events.*

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
val logger = LoggerFactory.getLogger(config.getString("bot.name"))!!

val cmds = CommandListener({ it?.getCommandPrefix() ?: getDefault("command_prefix") },
        { it?.getUserPermission(this) ?: Permission.NORMAL },
        { _, _ -> })

fun main(args: Array<String>) {
    logger.info("Starting ${config.getString("bot.name")} version ${config.getString("bot.build")}.")
    getDatabaseConnection("storage/storage.db")

    // normal
    cmds.add(CatCommand)
    cmds.add(GameCommand)
    cmds.add(JoinCommand)
    cmds.add(InviteCommand)
    cmds.add(LmgtfyCommand)
    cmds.add(MassAfkCommand)
    cmds.add(PingCommand)
    cmds.add(R8BallCommand)
    cmds.add(RockPaperScissorsCommand)
    cmds.add(StrawpollCommand)
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
    cmds.add(EditPermissionsCommand)
    // dev
    cmds.add(ShutdownCommand)

    val client = ClientBuilder().withToken(getKey("discord_bot_token")).build()
    client.dispatcher.registerListener(cmds)
    client.dispatcher.registerListener(GuildCreateHandler)
    client.dispatcher.registerListener(ReadyHandler)
    client.dispatcher.registerListener(MessageHandler)
    client.dispatcher.registerListener(NewUserHandler)
    client.dispatcher.registerListener(UserJoinEvent)
    client.dispatcher.registerListener(UserLeaveEvent)
    client.dispatcher.registerListener(UserMovedEvent)
    client.login()
}