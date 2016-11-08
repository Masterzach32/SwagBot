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
package net.masterzach32.swagbot.commands.mod

import net.masterzach32.commands4j.Command
import net.masterzach32.commands4j.Permission
import net.masterzach32.commands4j.getWrongArgumentsMessage
import net.masterzach32.commands4j.util.MetadataMessageBuilder
import net.masterzach32.commands4j.withAutoDeleteMessage
import net.masterzach32.swagbot.App
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.handle.obj.IUser
import sx.blah.discord.util.DiscordException
import sx.blah.discord.util.MissingPermissionsException
import sx.blah.discord.util.RequestBuffer

class PruneCommand : Command("Prune", "prune", "purge", permission = Permission.MOD) {

    override fun execute(cmdUsed: String, args: Array<String>, user: IUser, message: IMessage, channel: IChannel, permission: Permission): MetadataMessageBuilder? {
        if (args.size != 1)
            return getWrongArgumentsMessage(channel, this, cmdUsed)
        val x: Int
        try {
            x = Integer.parseInt(args[0])
        } catch (e: NumberFormatException) {
            return MetadataMessageBuilder(channel).withContent("Amount must be a number.")
        }
        if (x < 2 || x > 100)
            return MetadataMessageBuilder(channel).withContent("Invalid amount specified. Must prune between 2-100 messages.")
        val builder = withAutoDeleteMessage(MetadataMessageBuilder(channel).withContent("**Fetching the last $x message(s)...**"), 5)
        App.logger.info("$builder")
        RequestBuffer.request {
            try {
                message.delete()
                val response = builder.build()
                val list = channel.messages
                val deleted = list.deleteFromRange(1, x+1)
                for (msg in deleted)
                    App.logger.info("deleted: $msg")
                response.edit("${user.mention()} Removed the last **$x** messages.")
            } catch (e: DiscordException) {
                e.printStackTrace()
            } catch (e: MissingPermissionsException) {
                e.printStackTrace()
            }
        }
        return null
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("<int>", "The number of messages to prune, must be between 2 and 100")
    }
}