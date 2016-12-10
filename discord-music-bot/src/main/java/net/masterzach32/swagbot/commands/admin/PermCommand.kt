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
package net.masterzach32.swagbot.commands.admin

import net.masterzach32.commands4j.Command
import net.masterzach32.commands4j.MetadataMessageBuilder
import net.masterzach32.commands4j.Permission
import net.masterzach32.commands4j.getWrongArgumentsMessage
import net.masterzach32.swagbot.App
import net.masterzach32.swagbot.App.guilds
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.handle.obj.IUser

class PermCommand: Command("Change Permissions", "permission", "perm", permission = Permission.ADMIN) {

    val perms = {
        val p = Permission.values().toMutableList()
        p.remove(Permission.DEVELOPER)
        p
    }.invoke()

    override fun execute(cmdUsed: String, args: Array<String>, user: IUser, message: IMessage, channel: IChannel, permission: Permission): MetadataMessageBuilder? {
        val guild = guilds.getGuildSettings(message.guild)
        val builder = MetadataMessageBuilder(channel)
        val users = message.mentions
        message.roleMentions
                .forEach { message.guild.getUsersByRole(it)
                        .forEach { users.add(it) }
                }
        if (args.size < 2 || users.isEmpty())
            return getWrongArgumentsMessage(channel, this, cmdUsed)
        val perm = perms.filter { it.name == args[0] }
        if (users.contains(message.guild.owner) && perm[0].ordinal < Permission.ADMIN.ordinal)
            return builder.withContent("You can't change the owner's permission level below `ADMIN`").setAutoDelete(30)
        if (perm.isNotEmpty())
            users.forEach { guild.setUserPerms(it, perm[0]) }
        else if (permission == Permission.DEVELOPER && args[0] == Permission.DEVELOPER.name)
            users.forEach { guild.setUserPerms(it, Permission.DEVELOPER) }
        else
            return getWrongArgumentsMessage(channel, this, cmdUsed)
        return builder.withContent("Set $users permission(s) to `${perm[0]}`")
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("<permission> <list of mentioned users>", "Set a list of user's to have the specified permissions. Allowed permission values are $perms")
    }
}