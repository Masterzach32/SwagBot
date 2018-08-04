package xyz.swagbot.commands.mod

import net.masterzach32.commands4k.*
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.handle.obj.Permissions
import sx.blah.discord.util.EmbedBuilder
import sx.blah.discord.util.MessageHistory
import sx.blah.discord.util.RequestBuffer
import xyz.swagbot.Stats
import xyz.swagbot.commands.getWrongArgumentsMessage
import xyz.swagbot.logger
import xyz.swagbot.utils.*
import java.lang.Thread.sleep

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

    init {
        help.usage["<int>"] = "Delete the previous 2-99+ messages no older than two weeks."
    }

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent,
                         builder: AdvancedMessageBuilder): AdvancedMessageBuilder? {
        if (args.size != 1)
            return getWrongArgumentsMessage(builder, this, cmdUsed)
        event.channel.toggleTypingStatus()

        val embed = embedRed("Invalid amount specified. Must prune between 2-100 messages.")
        val x: Int
        try {
            x = Integer.parseInt(args[0])
        } catch (e: NumberFormatException) {
            return builder.withEmbed(embed)
        }
        if (x < 2 || x > 1000)
            return builder.withEmbed(embed)

        val history = RequestBuffer.request<MessageHistory> {
            event.channel.getMessageHistoryFrom(event.messageID, x+1)
        }.get()

        var deleted = 0
        for (subList in history.split(100).map { MessageHistory(it) })
            deleted += RequestBuffer.request<Int> { subList.bulkDelete().size }.get()
        Stats.MESSAGES_PRUNED.addStat(deleted)

        builder.withEmbed(embedBlue("Removed the last **$deleted** messages."))
        builder.withAutoDelete(5)
        return builder
    }
}