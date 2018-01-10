package xyz.swagbot.commands.mod

import net.masterzach32.commands4k.*
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.handle.obj.Permissions
import sx.blah.discord.util.EmbedBuilder
import sx.blah.discord.util.MessageHistory
import sx.blah.discord.util.RequestBuffer
import xyz.swagbot.commands.getWrongArgumentsMessage
import xyz.swagbot.logger
import xyz.swagbot.utils.BLUE
import xyz.swagbot.utils.RED

/*
 * SwagBot - Created on 9/2/2017
 * Author: Zach Kozar
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at the project root.
 */

/**
 * @author Zach Kozar
 * @version 9/2/2017
 */
object PruneCommand : Command("Prune", "prune", "purge", botPerm = Permission.MOD, scope = Command.Scope.GUILD,
        discordPerms = listOf(Permissions.MANAGE_MESSAGES)) {

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent,
                         builder: AdvancedMessageBuilder): AdvancedMessageBuilder? {
        if (args.size != 1)
            return getWrongArgumentsMessage(builder, this, cmdUsed)
        event.channel.toggleTypingStatus()

        val embed = EmbedBuilder().withColor(RED).withDesc("Invalid amount specified. Must prune between 2-100 messages.")
        val x: Int
        try {
            x = Integer.parseInt(args[0])
        } catch (e: NumberFormatException) {
            return builder.withEmbed(embed)
        }
        if (x < 2 || x > 100)
            return builder.withEmbed(embed)

        val history = event.channel.getMessageHistoryFrom(event.messageID, x+1)

        val deleted = RequestBuffer.request<MutableList<IMessage>> { MessageHistory(history.subList(1, x+1)).bulkDelete() }.get()
        for (msg in deleted)
            logger.debug("deleted: $msg")

        builder.withEmbed(embed.withColor(BLUE).withDesc("Removed the last **$x** messages."))
        RequestBuffer.request { event.message.delete() }
        val response = RequestBuffer.request<IMessage> { builder.build() }.get()
        Thread.sleep(5000)
        RequestBuffer.request { response.delete() }
        return null
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("<int>", "Delete between 2 and 100 messages no older than two weeks.")
    }
}