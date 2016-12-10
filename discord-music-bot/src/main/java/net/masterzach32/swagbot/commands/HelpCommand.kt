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

import net.masterzach32.commands4j.*
import net.masterzach32.commands4j.MetadataMessageBuilder
import net.masterzach32.swagbot.App
import net.masterzach32.swagbot.utils.*

import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.handle.obj.IUser
import java.util.*

class HelpCommand : Command("Help", "help", "h", permission = Permission.NONE) {

    override fun execute(cmdUsed: String, args: Array<String>, user: IUser, message: IMessage, channel: IChannel, permission: Permission): MetadataMessageBuilder {
        val builder: MetadataMessageBuilder
        if (args.isEmpty()) {
            if (!channel.isPrivate) {
                MetadataMessageBuilder(channel).withContent("${user.mention()} A list of commands has been sent to your direct messages!")
            }
            builder = MetadataMessageBuilder(channel.client.getOrCreatePMChannel(user))
            builder.withContent("Commands for SwagBot:\nNote you can only see the commands available to you with your permission **$permission** in **${message.guild.name}**\n```")
            App.cmds.getCommandList()
                    .filterNot { it.hidden || it.permission.ordinal > permission.ordinal }
                    .forEach { builder.appendContent("$DEFAULT_COMMAND_PREFIX${it.aliases[0]}\n") }
            builder.appendContent("```\n\n" +
                    "**Note**: Command prefixes may be different per guild!" +
                    "\n\n" +
                    "**Permissions**:\n ${Permission.values().toList()}" +
                    "\n\n" +
                    "To view more information for a command, use `${DEFAULT_COMMAND_PREFIX}help <command>`" +
                    "\n\n" +
                    "Check out the development for SwagBot at:\nhttps://github.com/Masterzach32/SwagBot" +
                    "\nHelp development of SwagBot by donating to my PayPal:\nhttps://paypal.me/ultimatedoge" +
                    "\nOr pledge a small amount on Patreon:\n<https://patreon.com/ultimatedoge>" +
                    "\n\n" +
                    "For more info on the bot and its commands:\n<http://masterzach32.net/projects/swagbot>" +
                    "\n\n" +
                    "Join SwagBot Hub:\nhttps://discord.me/swagbothub" +
                    "\n\n" +
                    "Want to add SwagBot to your server? Click the link below:\nhttps://discordapp.com/oauth2/authorize?client_id=217065780078968833&scope=bot&permissions=8")
        } else {
            builder = MetadataMessageBuilder(channel).withContent("No command found with alias `${args[0]}`")
            App.cmds.getCommandList()
                    .filter { it.aliases.contains(args[0]) }
                    .forEach {
                        builder.withContent("**${it.name}**: Aliases: `${it.aliases}` Permission Required: `${it.permission}`")
                        val map = HashMap<String, String>()
                        it.getCommandHelp(map)
                        map.forEach { k, v -> builder.appendContent("\n`${if (k.isEmpty()) "default" else k}` $v") }
                    }

        }
        return builder
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("", "Display a list of commands.")
        usage.put("<command>", "Display detailed information about that command.")
    }
}