package xyz.swagbot.api.game

import sx.blah.discord.handle.obj.IChannel

/*
 * SwagBot - Created on 11/17/2017
 * Author: Zach Kozar
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at the project root.
 */

/**
 * @author Zach Kozar
 * @version 11/17/2017
 */
object GameManager {

    private val games = mutableListOf<Game>()

    fun isGameRunning(channel: IChannel): Boolean {
        return synchronized(games) { games.any { it.channel == channel } }
    }

    fun isGameInProgress(channel: IChannel): Boolean {
        return synchronized(games) { isGameRunning(channel) && games.first { it.channel == channel }.isInProgress() }
    }

    fun addGame(game: Game) {
        synchronized(games) { games.add(game) }
    }

    fun getGame(channel: IChannel): Game {
        return synchronized(games) { games.first { it.channel == channel } }
    }

    fun endGame(game: Game) {
        synchronized(games) { games.remove(game) }
    }
}