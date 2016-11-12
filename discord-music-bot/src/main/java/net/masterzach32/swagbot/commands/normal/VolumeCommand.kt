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
package net.masterzach32.swagbot.commands.normal

import net.masterzach32.commands4j.Command
import net.masterzach32.commands4j.Permission
import net.masterzach32.commands4j.getBotLockedMessage
import net.masterzach32.commands4j.getWrongArgumentsMessage
import net.masterzach32.commands4j.util.MetadataMessageBuilder
import net.masterzach32.swagbot.App
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.handle.obj.IUser

class VolumeCommand: Command("Change Volume", "volume", "v") {

    override fun execute(cmdUsed: String, args: Array<String>, user: IUser, message: IMessage, channel: IChannel, permission: Permission): MetadataMessageBuilder? {
        val guild = App.guilds.getGuildSettings(message.guild)
        if(guild.botLocked)
            return getBotLockedMessage(channel)
        if(args.size > 1)
            return getWrongArgumentsMessage(channel, this, cmdUsed)
        val vol: Float
        if(args.isEmpty())
            return MetadataMessageBuilder(channel).withContent("Volume is currently set to ${guild.audioPlayer.volume*100}")
        try {
            vol = args[0].toFloat()
            if(vol < 0 || vol > 100)
                return MetadataMessageBuilder(channel).withContent("Invalid volume level, must be between 0 and 100.")
            App.setVolume(vol, message.guild)
            return MetadataMessageBuilder(channel).withContent("Set volume to **$vol**")
        } catch (e: NumberFormatException) {
            return MetadataMessageBuilder(channel).withContent("Amount must be a number.")
        }
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("", "Print the current volume.")
        usage.put("<int>", "Change the volume of the bots audio, must be between 0 and 100.")
    }
}