package xyz.swagbot.utils

import com.mashape.unirest.http.Unirest
import sx.blah.discord.api.IDiscordClient
import xyz.swagbot.audioPlayerManager
import xyz.swagbot.database.shutdownAudioPlayer
import xyz.swagbot.logger

/*
 * SwagBot - Created on 9/1/2017
 * Author: Zach Kozar
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at the project root.
 */

/**
 * @author Zach Kozar
 * @version 9/1/2017
 */

internal fun shutdown(client: IDiscordClient) {
    stop(client, ExitCode.EXITED)
}

internal fun shutdown(client: IDiscordClient, ec: ExitCode) {
    stop(client, ec)
}

private fun stop(client: IDiscordClient, ec: ExitCode) {
    logger.info("Shutting down audio player.")
    try {
        client.guilds.forEach { it.shutdownAudioPlayer() }
    } catch (t: Throwable) {
        logger.error("Could not shut down audio players gracefully: ${t.message}")
    }
    audioPlayerManager.shutdown()
    Unirest.shutdown()
    DailyUpdate.shutdown()
    Thread {
        logger.info("Attempting to log out of Discord.")
        client.logout()
        logger.info("Attempt successful, exiting.")
        exit(ec)
    }.start()
    Thread.sleep(60*1000)
    logger.info("Could not gracefully log out of discord. Exiting.")
    exit(ec)
}

private fun exit(ec: ExitCode) {
    System.exit(ec.code)
    logger.info("System.exit() is not functioning properly!")
}