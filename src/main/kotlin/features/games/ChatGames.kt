package xyz.swagbot.features.games

import discord4j.core.event.*
import io.facet.core.*
import kotlinx.coroutines.*

class ChatGames {

    companion object : EventDispatcherFeature<EmptyConfig, ChatGames>(
        "chatGames",
        requiredFeatures = emptyList()
    ) {

        override suspend fun EventDispatcher.install(
            scope: CoroutineScope,
            configuration: EmptyConfig.() -> Unit
        ): ChatGames {
            return ChatGames()
        }
    }
}
