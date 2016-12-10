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
package net.masterzach32.swagbot.commands

import net.masterzach32.commands4j.Command
import net.masterzach32.commands4j.MetadataMessageBuilder
import net.masterzach32.commands4j.Permission
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.handle.obj.IUser

class InviteCommand : Command("Invite SwagBot", "invite", permission = Permission.NONE) {

    override fun execute(cmdUsed: String, args: Array<String>, user: IUser, message: IMessage, channel: IChannel, permission: Permission): MetadataMessageBuilder? {
        return MetadataMessageBuilder(channel).withContent("Click the following link to add SwagBot to your server!")
                .appendContent("\n<https://discordapp.com/oauth2/authorize?client_id=217065780078968833&scope=bot&permissions=8>")
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("", "Post an invite link for SwagBot.")
    }
}