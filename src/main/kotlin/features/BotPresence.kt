package xyz.swagbot.features

import discord4j.core.event.*
import io.facet.core.*
import kotlinx.coroutines.*
import xyz.swagbot.features.music.*

class BotPresence private constructor() {

    companion object : EventDispatcherFeature<EmptyConfig, BotPresence>(
        keyName = "presence",
        requiredFeatures = listOf(Music)
    ) {

        override suspend fun EventDispatcher.install(scope: CoroutineScope, configuration: EmptyConfig.() -> Unit): BotPresence {
            return BotPresence()
        }
    }
}
