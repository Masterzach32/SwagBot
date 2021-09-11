package xyz.swagbot.features.stats

import discord4j.core.event.EventDispatcher
import io.facet.core.EmptyConfig
import io.facet.core.EventDispatcherFeature
import io.facet.core.features.ChatCommands
import kotlinx.coroutines.CoroutineScope

class Stats {

    companion object : EventDispatcherFeature<EmptyConfig, Stats>(
        keyName = "stats",
        requiredFeatures = listOf(ChatCommands)
    ) {

        override suspend fun EventDispatcher.install(
            scope: CoroutineScope,
            configuration: EmptyConfig.() -> Unit
        ): Stats {
            return Stats().also { feature ->

            }
        }
    }
}
