package xyz.swagbot.events

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.guild.GuildCreateEvent
import xyz.swagbot.database.sb_guilds
import xyz.swagbot.database.sql
import xyz.swagbot.database.sqlPool

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
class GuildCreateHandler : IListener<GuildCreateEvent> {

    override fun handle(event: GuildCreateEvent) {
        sqlPool.submit {
            sql {
                if (sb_guilds.select { sb_guilds.id eq event.guild.stringID }.firstOrNull() == null) {
                    xyz.swagbot.database.logger.info("Adding new guild to database: ${event.guild.stringID}")
                    sb_guilds.insert {
                        it[sb_guilds.id] = event.guild.stringID
                        it[sb_guilds.name] = event.guild.name
                        it[sb_guilds.command_prefix] = "~"
                        it[sb_guilds.volume] = 50
                        it[sb_guilds.locked] = false
                    }
                    commit()
                }
            }
        }
    }
}