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
import net.masterzach32.swagbot.music.player.AudioTrack
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.handle.obj.IUser
import sx.blah.discord.util.RequestBuffer

class SkipCommand: Command("Skip", "skip") {

    override fun execute(cmdUsed: String, args: Array<String>, user: IUser, message: IMessage, channel: IChannel, permission: Permission): MetadataMessageBuilder? {
        if (guilds.getGuildSettings(message.guild).botLocked)
            return getBotLockedMessage(channel)
        val builder = MetadataMessageBuilder(channel)
        val guild = guilds.getGuildSettings(message.guild)
        if (guild.audioPlayer.playlistSize == 0)
            return builder.withContent("**There are no songs to skip!**")
        if (guild.hasUserSkipped(user.id))
            return builder.withContent("**You have already voted to skip this song!**")
        guild.addSkipID(user)
        val vc = message.guild.connectedVoiceChannel
        if(vc != null)
            guild.maxSkips = ((vc.connectedUsers.size-1) * 2 / 3.0 + 0.5).toInt()
        val isBotCommander = message.guild.getRolesByName("Bot Commander")
                .filter { user.getRolesForGuild(message.guild).contains(it) }
                .isNotEmpty()
        if (guild.numUntilSkip() == 0 || isBotCommander) {
            val track = guild.audioPlayer.skip() as AudioTrack
            guild.resetSkipStats()
            if (guild.audioPlayer.playlistSize == 0 && message.client.ourUser.getDisplayName(message.guild) != "SwagBot")
                RequestBuffer.request { message.guild.setUserNickname(message.client.ourUser, "SwagBot") }
            return builder.withContent("Skipped **${track.title}**")
        } else
            return builder.withContent("**${guild.numUntilSkip()}** more votes required to skip the current song.")
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("", "Vote to skip the current song in the queue.")
    }
}