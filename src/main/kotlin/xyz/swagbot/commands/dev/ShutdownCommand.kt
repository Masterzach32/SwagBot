package xyz.swagbot.commands.dev

import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Command
import net.masterzach32.commands4k.Permission
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import xyz.swagbot.logger
import xyz.swagbot.utils.ExitCode
import xyz.swagbot.utils.shutdown

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
object ShutdownCommand : Command("Shutdown / Restart", "shutdown", "stop", "restart", hidden = true, scope = Command.Scope.ALL,
        botPerm = Permission.DEVELOPER) {

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent,
                         builder: AdvancedMessageBuilder): AdvancedMessageBuilder? {
        if (cmdUsed == "restart") {
            logger.info("Restarting")
            shutdown(event.client, ExitCode.RESTART_REQUESTED)
        } else {
            logger.info("Shutting down")
            shutdown(event.client)
        }
        return null
    }
}