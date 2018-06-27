package xyz.swagbot.commands.admin

import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Command
import net.masterzach32.commands4k.Permission
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.Permissions
import sx.blah.discord.util.EmbedBuilder
import xyz.swagbot.commands.getWrongArgumentsMessage
import xyz.swagbot.database.getAutoAssignRole
import xyz.swagbot.database.setAutoAssignRole
import xyz.swagbot.utils.BLUE
import xyz.swagbot.utils.RED
import xyz.swagbot.utils.getContent

/*
 * SwagBot - Created on 8/30/2017
 * Author: Zach Kozar
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at the project root.
 */

/**
 * @author Zach Kozar
 * @version 8/30/2017
 */
object AutoAssignRoleCommand : Command(
        "Auto Assign Role",
        "autoassignrole",
        "joinrole",
        "aar",
        scope = Command.Scope.GUILD,
        botPerm = Permission.ADMIN,
        discordPerms = listOf(Permissions.MANAGE_ROLES)
) {

    init {
        help.desc = "Auto-assign new server members a selected role."
        help.usage[""] = "Displays the current role to be given to new server members, if it has been set."
        help.usage["set <role>"] = "Set the role to be assigned to new server members."
        help.usage["remove"] = "Stops the currently set role from being assigned to new server members."
    }

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent,
                         builder: AdvancedMessageBuilder): AdvancedMessageBuilder {
        val embed = EmbedBuilder().withColor(BLUE)

        if (args.isEmpty()) {
            if (event.guild.getAutoAssignRole() != null) {
                embed.withDesc("Current auto assigned role is **${event.guild.getAutoAssignRole()!!.name}**")
            } else {
                embed.withColor(RED)
                embed.withDesc("There currently is no auto assign role set!")
            }
            return builder.withEmbed(embed)
        }

        if (args[0] == "set" && args.size >= 2) {
            val roleString = getContent(args, 1)
            val role = event.guild.getRolesByName(roleString).firstOrNull()
            if (role == null) {
                embed.withColor(RED)
                embed.withDesc("No role with the name **$roleString** exists!")
                return builder.withEmbed(embed)
            }
            event.guild.setAutoAssignRole(role)
            embed.withDesc("New users will be assigned the role **${role.name}** when they join this server.")
        } else if (args[0] == "remove") {
            event.guild.setAutoAssignRole(null)
            embed.withDesc("New users will no longer be automatically assigned a role.")
        } else
            return getWrongArgumentsMessage(builder, this, cmdUsed)

        return builder.withEmbed(embed)
    }
}