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
import net.masterzach32.commands4j.getWrongArgumentsMessage
import net.masterzach32.commands4j.MetadataMessageBuilder
import net.masterzach32.swagbot.App
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.handle.obj.IUser

class AnnounceTrackCommand: Command("Announce Track Start", "tannounce", "ta", permission = Permission.ADMIN) {

    override fun execute(cmdUsed: String, args: Array<String>, user: IUser, message: IMessage, channel: IChannel, permission: Permission): MetadataMessageBuilder? {
        val guild = App.guilds.getGuildSettings(message.guild)
        val builder = MetadataMessageBuilder(channel)
        if (args.isEmpty())
            guild.announce = !guild.announce
        else if (args[0].toLowerCase() == "true")
            guild.announce = true
        else if (args[0].toLowerCase() == "false")
            guild.announce = false
        else
            return getWrongArgumentsMessage(channel, this, cmdUsed)
        if(guild.announce)
            return builder.withContent("**SwagBot will now announce when a user's queued track starts.**")
        else
            return builder.withContent("**SwagBot will no longer message a user when their queued track starts.**")
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("[true/false]", "Toggle whether to message the user when their track starts. Defaults to **true**")
    }
}