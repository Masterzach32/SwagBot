package xyz.swagbot.events

import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.guild.role.RoleDeleteEvent
import xyz.swagbot.database.removeIAmRole
import xyz.swagbot.logger

/*
 * SwagBot - Created on 1/18/2018
 * Author: Zach Kozar
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at the project root.
 */

/**
 * @author Zach Kozar
 * @version 1/18/2018
 */
object RoleHandler : IListener<RoleDeleteEvent> {

    override fun handle(event: RoleDeleteEvent) {
        if (event.guild.removeIAmRole(event.role))
            logger.debug("Removed deleted role from sb_iam_roles.")
    }
}