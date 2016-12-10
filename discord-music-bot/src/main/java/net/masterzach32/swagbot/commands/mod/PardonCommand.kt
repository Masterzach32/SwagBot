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

import net.masterzach32.commands4j.*
import net.masterzach32.swagbot.utils.Utils
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.handle.obj.IUser
import sx.blah.discord.handle.obj.Permissions
import sx.blah.discord.util.RequestBuffer

class PardonCommand: Command("Pardon User", "pardon", permission = Permission.MOD) {

    override fun execute(cmdUsed: String, args: Array<String>, user: IUser, message: IMessage, channel: IChannel, permission: Permission): MetadataMessageBuilder? {
        if (!userHasPermission(user, message.guild, Permissions.BAN))
            return insufficientPermission(channel, Permissions.BAN)
        if(args.isEmpty())
            return getWrongArgumentsMessage(channel, this, cmdUsed)
        message.guild.bannedUsers
                .filter { it.name == Utils.getContent(args, 0) }
                .forEach { RequestBuffer.request { message.guild.pardonUser(it.id) } }
        return null
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("<user>", "The user to pardon in this server.")
    }
}