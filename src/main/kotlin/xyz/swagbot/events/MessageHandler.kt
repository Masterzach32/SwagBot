package xyz.swagbot.events

import net.masterzach32.commands4k.AdvancedMessageBuilder
import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.RequestBuffer

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
        if (event.author == event.client.ourUser || event.guild == null)
            return

        if (event.guild.stringID == "97342233241464832") {
            if (event.message.channel.stringID == "402224449367179264") {
                if (!event.message.embeds.isEmpty() || !event.message.attachments.isEmpty()) {
                    RequestBuffer.request {
                        AdvancedMessageBuilder(event.message.channel)
                                .withContent("${event.message.author} please don't post links or attachments in " +
                                        "${event.message.channel}")
                                .withAutoDelete(30)
                                .build()
                    }
                    RequestBuffer.request { event.message.delete() }
                }
            }
        }
    }
}