package xyz.swagbot.features.music.listeners

import discord4j.core.event.domain.guild.*
import io.facet.discord.event.*
import xyz.swagbot.features.music.*

class GuildLeaveListener(val feature: Music) : Listener<GuildDeleteEvent> {

    override suspend fun on(event: GuildDeleteEvent) {
        if (!event.isUnavailable)
            feature.deinitializeFor(event.guildId)
    }
}
