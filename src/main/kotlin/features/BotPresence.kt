package xyz.swagbot.features

import discord4j.core.event.*
import io.facet.core.*
import io.facet.discord.*
import xyz.swagbot.features.music.*

class BotPresence private constructor() {

    companion object : EventDispatcherFeature<EmptyConfig, BotPresence>(
        keyName = "presence",
        requiredFeatures = listOf(Music)
    ) {

        override fun install(dispatcher: EventDispatcher, configuration: EmptyConfig.() -> Unit): BotPresence {
            return BotPresence()
        }
    }
}
