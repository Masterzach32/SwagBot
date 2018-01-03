package xyz.swagbot.utils

import com.mashape.unirest.http.Unirest
import sx.blah.discord.api.IDiscordClient
import xyz.swagbot.audioPlayerManager
import xyz.swagbot.database.getAllAudioHandlers
import xyz.swagbot.database.saveTracksToStorage
import java.io.File

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
internal fun isUpdateAvailable(): Boolean {
    return false
}

internal fun exitAndUpdate(client: IDiscordClient) {
    ProcessBuilder("java -jar bin${File.separator}patcher.jar").start()
    stop(client)
}

internal fun stop(client: IDiscordClient) {
    getAllAudioHandlers().forEach { guildId, handler ->
        handler.saveTracksToStorage(client.getGuildByID(guildId.toLong()))
        handler.player.destroy()
    }
    audioPlayerManager.shutdown()
    client.logout()
    Unirest.shutdown()
    System.exit(0)
}