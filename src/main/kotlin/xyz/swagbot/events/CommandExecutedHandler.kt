package xyz.swagbot.events

import net.masterzach32.commands4k.events.CommandExecutedEvent
import sx.blah.discord.api.events.IListener
import xyz.swagbot.Stats

/*
 * SwagBot - Created on 5/16/2018
 * Author: Zach Kozar
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at the project root.
 */

/**
 * @author Zach Kozar
 * @version 5/16/2018
 */
object CommandExecutedHandler : IListener<CommandExecutedEvent> {

    override fun handle(event: CommandExecutedEvent) {
        Stats.COMMANDS_USED.addStat()
    }
}