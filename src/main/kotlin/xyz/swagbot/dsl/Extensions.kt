package xyz.swagbot.dsl

import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.handle.obj.IUser
import sx.blah.discord.handle.obj.StatusType

/*
 * SwagBot - Created on 8/30/2017
 * Author: Zach Kozar
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at the project root.
 */

/**
 * @author Zach Kozar
 * @version 8/30/2017
 */
fun IMessage.getAllUserMentions(): List<IUser> {
    if (mentionsEveryone())
        return guild.users
    val users = mutableListOf<IUser>()
    if (mentionsHere())
        guild.users.filter { it.presence.status != StatusType.OFFLINE }.forEach { users.add(it) }
    roleMentions.forEach { guild.getUsersByRole(it).filter { !users.contains(it) }.forEach { users.add(it) } }
    mentions.filter { !users.contains(it) }.forEach { users.add(it) }
    return users
}