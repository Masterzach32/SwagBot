package xyz.swagbot.features.games

import discord4j.core.event.*
import io.facet.core.*
import io.facet.discord.*

class ChatGames {

    companion object : EventDispatcherFeature<EmptyConfig, ChatGames>(
        "chatGames",
        requiredFeatures = emptyList()
    ) {

        override fun install(dispatcher: EventDispatcher, configuration: EmptyConfig.() -> Unit): ChatGames {
            return ChatGames()
        }
    }
}
