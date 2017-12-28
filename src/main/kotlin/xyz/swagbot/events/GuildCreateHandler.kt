package xyz.swagbot.events

import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.guild.GuildCreateEvent
import xyz.swagbot.database.*

/*
 * SwagBot - Created on 8/24/17
 * Author: zachk
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at the project root.
 */

/**
 * @author zachk
 * @version 8/24/17
 */
object GuildCreateHandler : IListener<GuildCreateEvent> {

    override fun handle(event: GuildCreateEvent) {
        if (!does_guild_entry_exist(event.guild.stringID)) {
            xyz.swagbot.database.logger.info("Adding new guild to database: ${event.guild.stringID}")
            create_guild_entry(event.guild)
        }
        event.guild.initializeAutioPlayer()
        event.guild.audioManager.audioProvider = event.guild.getAudioHandler()!!.audioProvider
    }
}