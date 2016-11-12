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
package net.masterzach32.swagbot.commands.admin

import net.masterzach32.commands4j.Command
import net.masterzach32.commands4j.Permission
import net.masterzach32.commands4j.util.MetadataMessageBuilder
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.handle.obj.IUser
import net.masterzach32.swagbot.App.guilds



class NickCommand: Command("Change Nickname to Song", "cns", permission = Permission.ADMIN){

    override fun execute(cmdUsed: String, args: Array<String>, user: IUser, message: IMessage, channel: IChannel, permission: Permission): MetadataMessageBuilder? {
        val guild = guilds.getGuildSettings(message.guild)
        val builder = MetadataMessageBuilder(channel)
        if (args.size === 0)
            guild.setChangeNick(!guild.shouldChangeNick())
        else if (args[0].toLowerCase() == "true")
            guild.setChangeNick(true)
        else if (args[0].toLowerCase() == "false")
            guild.setChangeNick(false)
        else
            guild.setChangeNick(!guild.shouldChangeNick())
        if(guild.shouldChangeNick())
            return builder.withContent("**SwagBot will now change its nickname based on the current song in the queue.**")
        else
            return builder.withContent("**SwagBot will no longer change its nickname based on the current song in the queue.**")
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("[true/false]", "Toggle whether to change the bot's nickname based on the current track. Defaults to **false**")
    }
}