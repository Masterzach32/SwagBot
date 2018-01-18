package xyz.swagbot.commands.normal

import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Command
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.IRole
import sx.blah.discord.util.EmbedBuilder
import sx.blah.discord.util.RequestBuffer
import xyz.swagbot.commands.getWrongArgumentsMessage
import xyz.swagbot.database.isRoleSelfAssignable
import xyz.swagbot.utils.BLUE
import xyz.swagbot.utils.RED
import xyz.swagbot.utils.getContent

/*
 * SwagBot - Created on 1/18/2018
 * Author: Zach Kozar
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at the project root.
 */

/**
 * @author Zach Kozar
 * @version 1/18/2018
 */
object IAmNotCommand : Command("I Am Not (Remove Role)", "iamnot", scope = Scope.GUILD) {

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent,
                         builder: AdvancedMessageBuilder): AdvancedMessageBuilder {
        if (args.isEmpty())
            return getWrongArgumentsMessage(builder ,this, cmdUsed)
        val embed = EmbedBuilder().withColor(RED)
        val role: IRole?
        if (event.message.roleMentions.isNotEmpty())
            role = event.message.roleMentions.first()
        else
            role = event.guild.getRolesByName(getContent(args, 0)).firstOrNull()
        if (role == null)
            embed.withDesc("That role does not exist!")
        else if (event.guild.isRoleSelfAssignable(role)) {
            RequestBuffer.request { event.author.removeRole(role) }
            embed.withColor(BLUE).withDesc("You no longer have the role **${role.name}**.")
        } else
            embed.withDesc("Sorry, I cannot remove that role.")
        return builder.withEmbed(embed)
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("<role>", "Remove the specified role, if it was self-assigned.")
    }
}