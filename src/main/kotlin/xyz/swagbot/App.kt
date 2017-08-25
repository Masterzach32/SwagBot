package xyz.swagbot

import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory
import sx.blah.discord.api.ClientBuilder
import xyz.swagbot.database.getDatabaseConnection
import xyz.swagbot.database.getKey
import xyz.swagbot.events.GuildCreateHandler
import xyz.swagbot.events.MessageHandler
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

fun main(args: Array<String>) {
    logger.info("Starting ${config.getString("bot.name")} version ${config.getString("bot.build")}.")
    getDatabaseConnection("storage/storage.db")

    val client = ClientBuilder().withToken(getKey("discord_bot_token")).build()
    client.dispatcher.registerListener(GuildCreateHandler())
    client.dispatcher.registerListener(ReadyHandler())
    client.dispatcher.registerListener(MessageHandler())
    client.login()
}