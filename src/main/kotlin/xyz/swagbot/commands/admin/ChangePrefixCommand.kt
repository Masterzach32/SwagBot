package xyz.swagbot.commands.admin

import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Command
import net.masterzach32.commands4k.Permission
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.EmbedBuilder
import xyz.swagbot.commands.getWrongArgumentsMessage
import xyz.swagbot.database.getCommandPrefix
import xyz.swagbot.database.setCommandPrefix
import xyz.swagbot.utils.BLUE

/*
 * SwagBot - Created on 8/31/2017
 * Author: Zach Kozar
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at the project root.
 */

/**
 * @author Zach Kozar
 * @version 8/31/2017
 */
object ChangePrefixCommand : Command("Change Prefix", "changeprefix", "cp", "prefix",
        scope = Command.Scope.GUILD, botPerm = Permission.ADMIN) {

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent,
                         builder: AdvancedMessageBuilder): AdvancedMessageBuilder {
        if (args.size != 1)
            return getWrongArgumentsMessage(builder, this, cmdUsed)

        val embed = EmbedBuilder().withColor(BLUE)

        event.guild.setCommandPrefix(args[0])

        embed.withDesc("Command prefix set to **${event.guild.getCommandPrefix()}**")

        return builder.withEmbed(embed)
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("<string>", "The new command prefix for this server.")
    }
}