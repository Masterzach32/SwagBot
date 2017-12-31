package xyz.swagbot.events

import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.guild.GuildLeaveEvent
import xyz.swagbot.logger

object GuildLeaveHandler : IListener<GuildLeaveEvent> {

    override fun handle(event: GuildLeaveEvent) {
        logger.info("SwagBot left guild: ${event.guild.name} (${event.guild.stringID})")
    }
}