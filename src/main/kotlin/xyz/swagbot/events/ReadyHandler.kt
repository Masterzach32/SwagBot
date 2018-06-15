package xyz.swagbot.events

import org.jetbrains.exposed.sql.selectAll
import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.ReadyEvent
import sx.blah.discord.util.RequestBuffer
import xyz.swagbot.database.getLastVoiceChannel
import xyz.swagbot.database.sb_guilds
import xyz.swagbot.database.sql
import xyz.swagbot.logger
import xyz.swagbot.status.StatusUpdate
import xyz.swagbot.utils.DailyUpdate
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

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
object ReadyHandler : IListener<ReadyEvent> {

    override fun handle(event: ReadyEvent) {
        // join all voice channels the bot was in before it was shut down
        sql {
            sb_guilds.selectAll()
                    .mapNotNull { it[sb_guilds.last_voice_channel] }
                    .mapNotNull { event.client.getVoiceChannelByID(it) }
                    .forEach { RequestBuffer.request { it.join() } }
        }

        logger.info("Startup complete.")

        StatusUpdate.init(event.client)
    }
}