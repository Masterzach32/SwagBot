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
import net.masterzach32.commands4j.util.MetadataMessageBuilder
import net.masterzach32.swagbot.App.guilds
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.handle.obj.IUser
import net.masterzach32.swagbot.music.player.AudioTrack


class QueueCommand: Command("Queue", "queue") {

    override fun execute(cmdUsed: String, args: Array<String>, user: IUser, message: IMessage, channel: IChannel, permission: Permission): MetadataMessageBuilder? {
        val player = guilds.getGuildSettings(message.guild).audioPlayer
        val builder = MetadataMessageBuilder(channel)
        var str = ""
        var pageNumber = 0
        if (player.playlist.isEmpty()) {
            return builder.withContent("There are no songs in the queue!")
        } else if (args.isEmpty())
            pageNumber = 0
        else {
            try {
                pageNumber = args[0].toInt()
                if (pageNumber > player.playlistSize / 15 + 1 || pageNumber < 1)
                    pageNumber = 0
                else
                    pageNumber = pageNumber - 1
            } catch (e: NumberFormatException) {
                return builder.withContent("You didn't provide a number!")
            }

        }

        str += "There are **" + (player.playlistSize - 1) + "** song(s) in queue.\n"
        str += "Currently Playing: **" + (player.playlist[0] as AudioTrack).title + "** (**" + (if ((player.playlist[0] as AudioTrack).user == null) "null" else (player.playlist[0] as AudioTrack).user.getDisplayName(message.guild)) + "**)\n"
        str += "Queue Page " + (pageNumber + 1) + ":\n"

        var i = pageNumber * 15 + 1
        while (i < player.playlistSize && i < (pageNumber + 1) * 15 + 1) {
            str += "**(" + i + ")** - " + (player.playlist[i] as AudioTrack).title + " (**" + (if ((player.playlist[i] as AudioTrack).user == null) "null" else (player.playlist[i] as AudioTrack).user.getDisplayName(message.guild)) + "**)\n"
            i++
        }
        return builder.withContent(str)
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("[page number]", "Display the queue page, defaults to page 1.")
    }
}