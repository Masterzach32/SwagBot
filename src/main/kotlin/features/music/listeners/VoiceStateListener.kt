package xyz.swagbot.features.music.listeners

import discord4j.core.event.domain.*
import io.facet.discord.event.*
import io.facet.discord.extensions.*
import xyz.swagbot.features.music.*

class VoiceStateListener(val feature: Music) : Listener<VoiceStateUpdateEvent> {

    override suspend fun on(event: VoiceStateUpdateEvent) {
        val currentVs = event.current
        if (currentVs.user.await().id == event.client.selfId.get() && currentVs.channelId.isPresent)
            feature.updateCurrentlyConnectedChannelFor(currentVs.guildId, currentVs.channelId.get())
    }
}
