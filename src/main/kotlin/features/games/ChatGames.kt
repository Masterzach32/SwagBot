package xyz.swagbot.features.games

import discord4j.core.*
import io.facet.core.*
import io.facet.discord.*

class ChatGames {

    companion object : DiscordClientFeature<EmptyConfig, ChatGames>(
        "chatGames",
        requiredFeatures = emptyList()
    ) {

        override fun install(client: GatewayDiscordClient, configuration: EmptyConfig.() -> Unit): ChatGames {
            return ChatGames()
        }
    }
}
