package xyz.swagbot.events

import net.masterzach32.commands4k.AdvancedMessageBuilder
import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.guild.GuildCreateEvent
import sx.blah.discord.util.RequestBuffer
import xyz.swagbot.database.*
import java.time.LocalDateTime
import java.time.ZoneId

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
        val now = LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond()
        val joined = RequestBuffer.request<Long> {
            event.guild.getJoinTimeForUser(event.client.ourUser).atZone(ZoneId.systemDefault()).toEpochSecond()
        }.get()

        if (now - joined < 30) {
            val builder = AdvancedMessageBuilder(event.guild.defaultChannel)
            builder.withContent("Thanks for adding me to your server! If you need help, check out the" +
                    " getting started guide on my website: https://swagbot.xyz/gettingstarted")
            RequestBuffer.request { builder.build() }
        }

        event.guild.initialize()
        logger.info("Guild ${event.guild.name} (${event.guild.stringID}) is ready to start receiving commands.")
    }
}