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
import net.masterzach32.swagbot.App
import net.masterzach32.swagbot.utils.Utils
import sx.blah.discord.handle.obj.*
import sx.blah.discord.util.DiscordException
import sx.blah.discord.util.MissingPermissionsException
import sx.blah.discord.util.RequestBuffer

class MigrateCommand: Command("Migrate", "migrate", "populate", "m", "move", permission = Permission.MOD) {

    override fun execute(cmdUsed: String, args: Array<String>, user: IUser, message: IMessage, channel: IChannel, permission: Permission): MetadataMessageBuilder? {
        if (!userHasPermission(user, message.guild, Permissions.VOICE_MOVE_MEMBERS))
            return insufficientPermission(channel, Permissions.VOICE_MOVE_MEMBERS)
        val from: IVoiceChannel?
        val to: IVoiceChannel?
        if(args.isEmpty()) {
            if(message.author.connectedVoiceChannels.size == 0)
                return MetadataMessageBuilder(channel).withContent("**Make sure you are in the channel you want to populate!**")

            from = message.guild.connectedVoiceChannel
            to = message.author.connectedVoiceChannels[0]

            if(from == null)
                return MetadataMessageBuilder(channel).withContent("**Make sure the bot is the channel that you want to migrate from!**")
        } else {
            val channels = Utils.delimitWithoutEmpty(Utils.getContent(args, 0), "\\|")
            if(channels.size != 2)
                return getWrongArgumentsMessage(channel, this, cmdUsed)

            from = message.guild.getVoiceChannelsByName(channels[0])[0]
            to = message.guild.getVoiceChannelsByName(channels[1])[0]

            if(from == null || to == null)
                return getWrongArgumentsMessage(channel, this, cmdUsed)
        }
        from.connectedUsers
                .forEach { RequestBuffer.request { it.moveToVoiceChannel(to) } }
        return null
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("", "Move everyone from the bot's voice channel to your voice channel.")
        usage.put("<from> | <to>", "Move everyone from one voice channel to another, case-sensitive.")
    }
}