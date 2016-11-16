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
package net.masterzach32.swagbot.commands.music

import net.masterzach32.commands4j.Command
import net.masterzach32.commands4j.Permission
import net.masterzach32.commands4j.getBotLockedMessage
import net.masterzach32.commands4j.MetadataMessageBuilder
import net.masterzach32.swagbot.App.guilds
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.handle.obj.IUser

class SkipToCommand: Command("Skip To", "skipto", permission = Permission.MOD) {

    override fun execute(cmdUsed: String, args: Array<String>, user: IUser, message: IMessage, channel: IChannel, permission: Permission): MetadataMessageBuilder? {
        if (guilds.getGuildSettings(message.guild).botLocked)
            return getBotLockedMessage(channel)
        val guild = guilds.getGuildSettings(message.guild)
        val builder = MetadataMessageBuilder(channel)
        if (args.isEmpty())
            return builder.withContent("You must specify the queue number to skip to.")
        val skipped = guild.audioPlayer.skipTo(args[0].toInt())
        return builder.withContent("Skipped ${skipped.size} songs")
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("<int>", "Skip to the specified position in the queue.")
    }
}