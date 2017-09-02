package xyz.swagbot.utils

import sx.blah.discord.api.IDiscordClient

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

internal fun runUpdater() {

}

internal fun stop(client: IDiscordClient) {
    client.logout()
    System.exit(0)
}