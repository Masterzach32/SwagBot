package xyz.swagbot.events

import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.guild.member.UserJoinEvent
import xyz.swagbot.database.getAutoAssignRole

/*
 * SwagBot - Created on 8/31/2017
 * Author: Zach Kozar
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at the project root.
 */

/**
 * @author Zach Kozar
 * @version 8/31/2017
 */
object NewUserHandler : IListener<UserJoinEvent> {

    override fun handle(event: UserJoinEvent) {
        val role = event.guild.getAutoAssignRole() ?: return
        event.user.addRole(role)
    }
}