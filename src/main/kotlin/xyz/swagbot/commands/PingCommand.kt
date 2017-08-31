package xyz.swagbot.commands

import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Command
import net.masterzach32.commands4k.Permission
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

/*
 * SwagBot - Created on 8/26/2017
 * Author: Zach Kozar
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at the project root.
 */

/**
 * @author Zach Kozar
 * @version 8/26/2017
 */
object PingCommand : Command("Ping", "ping") {

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent, permission: Permission): AdvancedMessageBuilder {
        return AdvancedMessageBuilder(event.channel).withContent("Pong!")
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {

    }
}