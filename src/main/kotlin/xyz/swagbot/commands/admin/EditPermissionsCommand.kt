package xyz.swagbot.commands.admin

import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Command
import net.masterzach32.commands4k.Permission
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.EmbedBuilder
import xyz.swagbot.commands.getWrongArgumentsMessage
import xyz.swagbot.database.*
import xyz.swagbot.dsl.getAllUserMentions
import xyz.swagbot.utils.BLUE
import xyz.swagbot.utils.RED

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
object EditPermissionsCommand : Command("Edit Permissions", "permission", "perm", "changep",
        scope = Command.Scope.GUILD, botPerm = Permission.ADMIN) {

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent,
                         builder: AdvancedMessageBuilder): AdvancedMessageBuilder? {
        val embed = EmbedBuilder().withColor(BLUE)
        val users = event.message.getAllUserMentions()

        if (args.size < 2 || users.isEmpty())
            return getWrongArgumentsMessage(builder, this, cmdUsed)

        val permToSet = Permission.values().firstOrNull { it.name == args[0].capitalize() }
        if (permToSet == null)
            return builder.withEmbed(embed.withColor(RED).withDesc("That permission level doesn't exist. " +
                    "Allowed permission levels are ${getPerms()}"))
        if ((permToSet == Permission.DEVELOPER && event.author.getBotPermission(event.guild) != Permission.DEVELOPER))
            return null

        users.forEach {
            embed.appendField("${it.name}#${it.discriminator}",
                    "${it.getBotPermission(event.guild)} -> $permToSet",
                    true)
            it.setBotPermission(event.guild, permToSet)
        }

        return builder.withEmbed(embed)
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("<permission> <list of mentioned users>", "Set a list of user's to have the specified permissions. " +
                "Allowed permission values are ${getPerms()}")
    }

    private fun getPerms(): List<Permission> {
        val tmp = Permission.values().toMutableList()
        tmp.remove(Permission.DEVELOPER)
        return tmp
    }
}