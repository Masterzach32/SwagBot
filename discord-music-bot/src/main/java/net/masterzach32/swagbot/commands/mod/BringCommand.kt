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
import net.masterzach32.commands4j.util.MetadataMessageBuilder
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.handle.obj.IUser

class BringCommand: Command("Bring Users", "bring", permission = Permission.MOD) {

    override fun execute(cmdUsed: String, args: Array<String>, user: IUser, message: IMessage, channel: IChannel, permission: Permission): MetadataMessageBuilder? {
        if(message.author.connectedVoiceChannels.size == 0)
            return MetadataMessageBuilder(channel).withContent("**You need to be in a voice channel to summon users.**")
        val vc = message.author.connectedVoiceChannels[0]
        message.guild.users
                .filter { it.connectedVoiceChannels.size == 1 }
                .forEach { it.moveToVoiceChannel(vc) }
        return MetadataMessageBuilder(vc).withContent("Moved everyone to **$channel**.")
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("", "Brings all users currently connected to a voice channel to you.")
    }
}