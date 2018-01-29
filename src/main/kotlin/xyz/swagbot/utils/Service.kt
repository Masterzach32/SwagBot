package xyz.swagbot.utils

import com.mashape.unirest.http.Unirest
import sx.blah.discord.api.IDiscordClient
import xyz.swagbot.audioPlayerManager
import xyz.swagbot.database.getAllAudioHandlers
import xyz.swagbot.database.saveTracksToStorage
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
    stop(client)
    exit(ExitCode.EXITED)
}

internal fun shutdown(client: IDiscordClient, ec: ExitCode) {
    stop(client)
    exit(ec)
}

private fun stop(client: IDiscordClient) {
    try {
        client.guilds.forEach { it.shutdownAudioPlayer() }
    } catch (t: Throwable) {
        logger.error("Could not shut down audio players gracefully: ${t.message}")
    }
    audioPlayerManager.shutdown()
    client.logout()
    Unirest.shutdown()
}

private fun exit(ec: ExitCode) {
    System.exit(ec.code)
}
