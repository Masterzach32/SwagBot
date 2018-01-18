package xyz.swagbot.commands.normal

import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Command
import net.masterzach32.commands4k.Permission
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.IRole
import sx.blah.discord.util.EmbedBuilder
import sx.blah.discord.util.RequestBuffer
import xyz.swagbot.commands.getWrongArgumentsMessage
import xyz.swagbot.database.*
import xyz.swagbot.utils.BLUE
import xyz.swagbot.utils.RED
import xyz.swagbot.utils.getContent

/*
 * SwagBot - Created on 1/17/2018
 * Author: Zach Kozar
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at the project root.
 */

/**
 * @author Zach Kozar
 * @version 1/17/2018
 */
object IAmCommand : Command("I Am (Request Role)", "iam", scope = Scope.GUILD) {

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent,
                         builder: AdvancedMessageBuilder): AdvancedMessageBuilder {
        if (args.isEmpty())
            return getWrongArgumentsMessage(builder ,this, cmdUsed)
        val embed = EmbedBuilder().withColor(BLUE)
        if (args[0] == "list") {
            val list = event.guild.getIAmRoleList()
            if (list.isNotEmpty()) {
                embed.withDesc("These roles may be self-assigned: ${list.map { it?.name }}.")
            } else
                embed.withDesc("There are no roles that can be self-assigned.")
        } else if (args[0] == "add" && event.author.getBotPermission(event.guild) == Permission.ADMIN) {
            val role: IRole?
            if (event.message.roleMentions.isNotEmpty())
                role = event.message.roleMentions.first()
            else
                role = event.guild.getRolesByName(getContent(args, 1)).firstOrNull()
            if (role == null)
                embed.withColor(RED).withDesc("Sorry, I could not find that role.")
            else if (event.guild.addIAmRole(role))
                embed.withDesc("Added role **${role.name}** to the self-assignable list.")
            else
                embed.withColor(RED).withDesc("That role is already on the self-assignable list.")
        } else if (args[0] == "remove" && event.author.getBotPermission(event.guild) == Permission.ADMIN) {
            val role: IRole?
            if (event.message.roleMentions.isNotEmpty())
                role = event.message.roleMentions.first()
            else
                role = event.guild.getRolesByName(getContent(args, 1)).firstOrNull()
            if (role == null)
                embed.withColor(RED).withDesc("Sorry, I could not find that role.")
            else if (event.guild.removeIAmRole(role))
                embed.withDesc("Removed role **${role.name}** from the self-assignable list.")
            else
                embed.withColor(RED).withDesc("That role is not on the self-assignable list.")
        } else {
            val role: IRole?
            if (event.message.roleMentions.isNotEmpty())
                role = event.message.roleMentions.first()
            else
                role = event.guild.getRolesByName(getContent(args, 0)).firstOrNull()
            if (role == null)
                embed.withColor(RED).withDesc("That role does not exist!")
            else if (event.guild.isRoleSelfAssignable(role)) {
                RequestBuffer.request { event.author.addRole(role) }
                embed.withDesc("You now have the role **${role.name}**.")
            } else
                embed.withColor(RED).withDesc("That role cannot be self-assigned.")
        }
        return builder.withEmbed(embed)
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("<role>", "Assign yourself a role.")
        usage.put("list", "List all roles that can be self-assigned.")
        usage.put("add <role>", "Add a role to the self-assign list. Type the role name or @mention it.")
        usage.put("remove <role>", "Remove a role from the self-assign list. Type the role name or @mention it.")
    }
}