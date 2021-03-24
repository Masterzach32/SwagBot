package xyz.swagbot.features.stats

import discord4j.core.event.*
import io.facet.core.*
import io.facet.discord.*
import io.facet.discord.commands.*

class Stats {

    companion object : EventDispatcherFeature<EmptyConfig, Stats>(
        keyName = "stats",
        requiredFeatures = listOf(ChatCommands)
    ) {

        override fun install(dispatcher: EventDispatcher, configuration: EmptyConfig.() -> Unit): Stats {
            return Stats().also { feature ->

            }
        }
    }
}
