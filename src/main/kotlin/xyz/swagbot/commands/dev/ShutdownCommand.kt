package xyz.swagbot.commands.dev

import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Command
import net.masterzach32.commands4k.Permission
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import xyz.swagbot.logger
import xyz.swagbot.utils.stop

/*
 * SwagBot - Created on 9/1/2017
 * Author: Zach Kozar
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at the project root.
 */

/**
 * @author Zach Kozar
 * @version 9/1/2017
 */
object ShutdownCommand : Command("Shutdown", "shutdown", "stop", hidden = true, scope = Command.Scope.ALL,
        botPerm = Permission.DEVELOPER) {

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent,
                         builder: AdvancedMessageBuilder): AdvancedMessageBuilder? {
        logger.info("Shutting down")
        stop(event.client)
        return null
    }
}