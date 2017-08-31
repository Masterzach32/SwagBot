package xyz.swagbot.commands.admin

import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Command
import net.masterzach32.commands4k.Permission
import net.masterzach32.commands4k.getWrongArgumentsMessage
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.EmbedBuilder
import xyz.swagbot.database.getUserPermission
import xyz.swagbot.database.setUserPermission
import xyz.swagbot.dsl.getAllUserMentions
import xyz.swagbot.utils.BLUE

/*
 * SwagBot - Created on 8/27/2017
 * Author: Zach Kozar
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at the project root.
 */

/**
 * @author Zach Kozar
 * @version 8/27/2017
 */
object EditPermissionsCommand : Command("Edit Permissions", "permission", "perm", "changep", permission = Permission.ADMIN) {

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent, permission: Permission): AdvancedMessageBuilder {
        val builder = AdvancedMessageBuilder(event.channel)
        val embed = EmbedBuilder().withColor(BLUE)
        val users = event.message.getAllUserMentions()

        if (args.size < 2 || users.isEmpty())
            return getWrongArgumentsMessage(event.channel, this, cmdUsed)

        val permToSet = getPerms().first { it.name == args[0] }

        users.forEach {
            embed.appendField("${it.name}#${it.discriminator}", "${event.guild.getUserPermission(it)} -> $permToSet", true)
            event.guild.setUserPermission(it, permToSet)
        }

        return builder.withEmbed(embed)
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("<permission> <list of mentioned users>", "Set a list of user's to have the specified permissions. " +
                "Allowed permission values are ${getPerms()}")
    }

    fun getPerms(): List<Permission> {
        val tmp = Permission.values().toMutableList()
        tmp.remove(Permission.DEVELOPER)
        return tmp
    }
}