package xyz.swagbot.events

import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Permission
import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.EmbedBuilder
import sx.blah.discord.util.MissingPermissionsException
import sx.blah.discord.util.RequestBuffer
import xyz.swagbot.cmds
import xyz.swagbot.database.getCommandPrefix
import xyz.swagbot.database.getUserPermission
import xyz.swagbot.logger
import xyz.swagbot.utils.RED

/*
 * SwagBot - Created on 8/25/17
 * Author: zachk
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at the project root.
 */

/**
 * @author zachk
 * @version 8/25/17
 */
object MessageHandler : IListener<MessageReceivedEvent> {

    override fun handle(event: MessageReceivedEvent) {
        if (event.message.channel.stringID == "97342233241464832") {
            if (!event.message.embeds.isEmpty() || !event.message.attachments.isEmpty()) {
                AdvancedMessageBuilder(event.message.channel)
                        .withContent("${event.message.author} please don't post links or attachments in " +
                                "${event.message.channel}")
                        .withAutoDelete(30)
                        .build()
                event.message.delete()
                return
            }
        }
    }
}