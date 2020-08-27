package xyz.swagbot.features

import discord4j.core.*
import discord4j.core.`object`.presence.*
import discord4j.core.event.domain.lifecycle.*
import io.facet.core.*
import io.facet.discord.*
import io.facet.discord.extensions.*
import xyz.swagbot.features.music.*

class BotPresence private constructor() {

    companion object : DiscordClientFeature<EmptyConfig, BotPresence>("presence", listOf(Music)) {

        override fun install(client: GatewayDiscordClient, configuration: EmptyConfig.() -> Unit): BotPresence {
            return BotPresence().also { feature ->
                client.listener<ReadyEvent> {
                    client.updatePresence(Presence.online(Activity.listening("~help"))).await()
                }
            }
        }
    }
}
