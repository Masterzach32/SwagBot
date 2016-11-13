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
import net.masterzach32.commands4j.getWrongArgumentsMessage
import net.masterzach32.commands4j.util.MetadataMessageBuilder
import net.masterzach32.swagbot.utils.Utils
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.handle.obj.IUser
import java.util.*

class ChooseCommand: Command("Choose from Group", "choose") {

    override fun execute(cmdUsed: String, args: Array<String>, user: IUser, message: IMessage, channel: IChannel, permission: Permission): MetadataMessageBuilder? {
        val choices = Utils.delimitWithoutEmpty(Utils.getContent(args, 0), "\\|")
        if (choices.size < 2)
            return getWrongArgumentsMessage(channel, this, cmdUsed)
        return MetadataMessageBuilder(channel).withContent("I choose **${choices[Random().nextInt(choices.size)]}**")
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("<option 1> | <option 2> [| [option 3]]", "Randomly picks one of the options given, separated by pipe characters. Must provide at least 2 options.")
    }
}