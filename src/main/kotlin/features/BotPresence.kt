package xyz.swagbot.features

import discord4j.core.event.EventDispatcher
import io.facet.core.EmptyConfig
import io.facet.core.EventDispatcherFeature
import kotlinx.coroutines.CoroutineScope
import xyz.swagbot.features.music.Music

class BotPresence private constructor() {

    companion object : EventDispatcherFeature<EmptyConfig, BotPresence>(
        keyName = "presence",
        requiredFeatures = listOf(Music)
    ) {

        override suspend fun EventDispatcher.install(
            scope: CoroutineScope,
            configuration: EmptyConfig.() -> Unit
        ): BotPresence {
            return BotPresence()
        }
    }
}
