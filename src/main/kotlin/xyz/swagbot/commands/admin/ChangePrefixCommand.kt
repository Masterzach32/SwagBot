package xyz.swagbot.commands.admin

import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Command
import net.masterzach32.commands4k.Permission
import net.masterzach32.commands4k.getWrongArgumentsMessage
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.EmbedBuilder
import sx.blah.discord.util.MessageBuilder
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
object ChangePrefixCommand : Command("Change Prefix", "changeprefix", "cp", permission = Permission.ADMIN) {

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent, permission: Permission): MessageBuilder? {
        if (args.size != 1)
            return getWrongArgumentsMessage(event.channel, this, cmdUsed)

        val builder = AdvancedMessageBuilder(event.channel)
        val embed = EmbedBuilder().withColor(BLUE)

        event.guild.setCommandPrefix(args[0])

        embed.withDesc("Command prefix set to **${event.guild.getCommandPrefix()}**")

        return builder.withEmbed(embed)
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("<string>", "The new command prefix for this server.")
    }
}