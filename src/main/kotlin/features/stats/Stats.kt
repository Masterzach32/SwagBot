package xyz.swagbot.features.stats

import discord4j.core.*
import io.facet.core.*
import io.facet.discord.*
import io.facet.discord.commands.*

class Stats {

    companion object : DiscordClientFeature<EmptyConfig, Stats>(
        keyName = "stats",
        requiredFeatures = listOf(ChatCommands)
    ) {

        override fun install(client: GatewayDiscordClient, configuration: EmptyConfig.() -> Unit): Stats {
            return Stats().also { feature ->

            }
        }
    }
}
