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
package net.masterzach32.swagbot.commands.dev

import net.masterzach32.commands4j.Command
import net.masterzach32.commands4j.Permission
import net.masterzach32.commands4j.getWrongArgumentsMessage
import net.masterzach32.commands4j.util.MetadataMessageBuilder
import net.masterzach32.swagbot.App
import net.masterzach32.swagbot.utils.Utils
import sx.blah.discord.handle.impl.obj.VoiceChannel
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.handle.obj.IUser

class GameCommand: Command("Switch Channel per Game", "game", permission = Permission.ADMIN) {

    override fun execute(cmdUsed: String, args: Array<String>, user: IUser, message: IMessage, channel: IChannel, permission: Permission): MetadataMessageBuilder? {
        val listener = App.guilds.getGuildSettings(message.guild).statusListener
        if(args.size == 0)
            return getWrongArgumentsMessage(channel, this, cmdUsed)
        val command = args[0]
        val builder = MetadataMessageBuilder(channel)
        if (command == "enable") {
            listener.setEnabled(true)
            builder.withContent("You have enabled voice-channel switching based on your detected game!")
            if(listener.defaultChannel == null || listener.defaultChannel == "")
                builder.appendContent("\nNow you need to set a default voice channel by using `~game set-def-channel <voice channel name or id>`")
        } else if (command == "disable") {
            listener.setEnabled(false)
            builder.withContent("Disabled voice channel switching.")
        } else if (command == "set-def-channel") {
            val vc = Utils.getContent(args, 1)
            listener.setDefaultChannel(message.guild.voiceChannels.filter { it.name == vc }
                    .firstOrNull() ?: message.guild.getVoiceChannelByID(vc))
            if(listener.defaultChannel != null && listener.defaultChannel != "") {
                builder.withContent("Set default channel to ${listener.defaultChannel}")
                if(!listener.hasEntries())
                    builder.withContent("\nNow you need to add some games by using `~game add <game> | <voice channel name or id>`")
            } else
                builder.withContent("Could not find voice channel **$vc**")
        } else if (command == "list") {
            builder.withContent("Currently registered games:\n${listener.listEntries()}")
        } else if (command == "add") {
            for(i in args.indices) {
                if(args[i] == "|") {
                    val game = Utils.getContent(args, 1, i)
                    val vc = message.guild.voiceChannels.filter { it.name == Utils.getContent(args, i + 1) }
                            .firstOrNull() ?: message.guild.getVoiceChannelByID(Utils.getContent(args, i + 1))
                    if(vc != null) {
                        listener.addEntry(game, vc)
                        builder.withContent("**Added/Edited game trigger: **$game** assigned to **$vc**.")
                    } else
                        builder.withContent("Could not find voice channel **${Utils.getContent(args, i + 1)}")
                }
            }
        } else
            return getWrongArgumentsMessage(channel, this, cmdUsed)
        return builder
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("<enable / disable>", "Enable or disable this feature.")
        usage.put("set-def-channel <voice channel>", "Set the default voice channel.")
        usage.put("list", "List all registered games.")
        usage.put("add <game> | <voice channel name / id>", "Registers a game / voice channel pair with the bot.")
    }
}