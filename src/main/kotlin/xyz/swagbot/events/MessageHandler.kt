package xyz.swagbot.events

import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import xyz.swagbot.database.getCommandPrefix

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
class MessageHandler : IListener<MessageReceivedEvent> {

    override fun handle(event: MessageReceivedEvent) {
        if (!event.message.content.startsWith(event.guild.getCommandPrefix()))
            return

        val command: String
        val params: Array<String>

        val tmp = event.message.content.substring(1).split(" ").toTypedArray()
        command = tmp[0]
        params = tmp.copyOfRange(1, tmp.size)


    }
}