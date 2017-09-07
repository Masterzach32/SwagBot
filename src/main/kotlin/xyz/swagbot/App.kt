package xyz.swagbot

import com.typesafe.config.ConfigFactory
import net.masterzach32.commands4k.CommandManager
import org.slf4j.LoggerFactory
import sx.blah.discord.api.ClientBuilder
import xyz.swagbot.commands.*
import xyz.swagbot.commands.admin.*
import xyz.swagbot.commands.dev.ShutdownCommand
import xyz.swagbot.commands.mod.*
import xyz.swagbot.commands.normal.*
import xyz.swagbot.database.*
import xyz.swagbot.events.GuildCreateHandler
import xyz.swagbot.events.MessageHandler
import xyz.swagbot.events.NewUserHandler
import xyz.swagbot.events.ReadyHandler

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

val config = ConfigFactory.load()
val logger = LoggerFactory.getLogger(config.getString("bot.name"))

val cmds = CommandManager()

fun main(args: Array<String>) {
    logger.info("Starting ${config.getString("bot.name")} version ${config.getString("bot.build")}.")
    getDatabaseConnection("storage/storage.db")

    sql { create(sb_defaults, sb_guilds, sb_permissions) }

    // normal
    cmds.add(HelpCommand)
    cmds.add(PingCommand)
    cmds.add(VoiceCommand)
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
    client.dispatcher.registerListener(GuildCreateHandler)
    client.dispatcher.registerListener(ReadyHandler)
    client.dispatcher.registerListener(MessageHandler)
    client.dispatcher.registerListener(NewUserHandler)
    client.login()
}