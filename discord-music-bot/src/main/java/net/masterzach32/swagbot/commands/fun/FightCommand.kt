/*
    SwagBot-java
    Copyright (C) 2016 Zach Kozar

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc.,
    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package net.masterzach32.swagbot.commands.`fun`

import net.masterzach32.commands4j.Command
import net.masterzach32.commands4j.Permission
import net.masterzach32.commands4j.getWrongArgumentsMessage
import net.masterzach32.commands4j.MetadataMessageBuilder
import net.masterzach32.swagbot.utils.BotConfig
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.handle.obj.IUser
import sx.blah.discord.handle.obj.Presences
import sx.blah.discord.util.RequestBuffer
import java.util.*

class FightCommand(val prefs: BotConfig): Command("Fight", "fight") {

    override fun execute(cmdUsed: String, args: Array<String>, user: IUser, message: IMessage, channel: IChannel, permission: Permission): MetadataMessageBuilder? {
        val users = ArrayList<IUser>()
        if (message.mentionsEveryone())
            message.guild.users
                    .forEach { users.add(it) }
        else if (message.mentionsHere())
            message.guild.users
                    .filter { it.presence == Presences.ONLINE }
                    .forEach { users.add(it) }
        else if (message.roleMentions.isNotEmpty())
            message.roleMentions
                    .forEach { message.guild.getUsersByRole(it)
                            .forEach { users.add(it) } }
        else
            message.mentions
                    .forEach { users.add(it) }
        if (users.size < 2)
            return getWrongArgumentsMessage(channel, this, cmdUsed)
        MetadataMessageBuilder(channel).withContent("**Let the beawl begin!**").build()
        Thread.sleep(1000)
        while (users.size > 1) {
            var str = ""
            for (j in users.indices)
                if (j < users.size - 1)
                    str += "**${users[j].getDisplayName(message.guild)}**, "
                else
                    str += "and **${users[j].getDisplayName(message.guild)}** are fighting!"
            val msg = MetadataMessageBuilder(channel).withContent(str).build()
            Thread.sleep(1500)
            RequestBuffer.request {
                var dead: IUser
                do {
                    dead = users[Random().nextInt(users.size)]
                } while (dead.id == "148604482492563456")
                users.remove(dead)
                val killer = users[Random().nextInt(users.size)]
                var result = prefs.fightSituations[Random().nextInt(prefs.fightSituations.size)]
                result = result.replace("{killed}", "**${dead.getDisplayName(message.guild)}**")
                result = result.replace("{killer}", "**${killer.getDisplayName(message.guild)}**")
                msg?.edit(result)
            }
        }
        return MetadataMessageBuilder(channel).withContent("${users[0]} **won the brawl!**")
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("<user 1> <user 2> [user 3]", "Make multiple users fight! Use @mention to list users.")
    }
}