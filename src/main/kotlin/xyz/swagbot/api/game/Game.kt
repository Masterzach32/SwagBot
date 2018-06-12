package xyz.swagbot.api.game

import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IUser
import xyz.swagbot.Stats

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
abstract class Game(name: String, val channel: IChannel, val users: MutableList<IUser>) : Thread("$name:${channel.guild.stringID}") {

    val guild = channel.guild!!
    protected var inProgress = false

    open fun isInProgress(): Boolean = inProgress

    abstract fun getJoinMessage(user: IUser): String

    @Synchronized
    open fun addUser(user: IUser): Boolean {
        if (users.contains(user))
            return false
        users.add(user)
        return true
    }

    fun finish() {
        GameManager.endGame(this)
        Stats.GAMES_PLAYED.addStat()
    }
}