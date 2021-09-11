package xyz.swagbot.features.games

import discord4j.core.event.EventDispatcher
import io.facet.core.EmptyConfig
import io.facet.core.EventDispatcherFeature
import kotlinx.coroutines.CoroutineScope

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
