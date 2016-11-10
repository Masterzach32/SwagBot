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
import net.masterzach32.commands4j.util.MetadataMessageBuilder
import net.masterzach32.swagbot.App
import net.masterzach32.swagbot.utils.Utils
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.handle.obj.IUser
import sx.blah.discord.handle.obj.IVoiceChannel

class SummonCommand: Command("Summon Bot", "summon", "s") {

    override fun execute(cmdUsed: String, args: Array<String>, user: IUser, message: IMessage, channel: IChannel, permission: Permission): MetadataMessageBuilder? {
        if(App.guilds.getGuildSettings(message.guild).isBotLocked)
            return getBotLockedMessage(channel)
        val vc: IVoiceChannel
        if(args.size > 0) {
            vc = message.guild.getVoiceChannelsByName(Utils.getContent(args, 0))[0]
        } else {
            if(user.connectedVoiceChannels.size == 0)
                return MetadataMessageBuilder(channel).withContent("**You need to be in a voice channel to summon the bot.**")
            vc = user.connectedVoiceChannels[0]
        }
        vc?.join()
        return MetadataMessageBuilder(channel).withContent("Joined **$vc**.")
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("", "Summon the bot to the voice channel you are connected to.")
        usage.put("<voice channel>", "Summon the bot to the specified voice channel.")
    }
}