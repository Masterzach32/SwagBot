package xyz.swagbot.features

import discord4j.core.event.*
import io.facet.core.*
import kotlinx.coroutines.*
import xyz.swagbot.features.guilds.*

class Market(config: Config) {

    class Config

    companion object : EventDispatcherFeature<Config, Market>("market", listOf(GuildStorage)) {

        override suspend fun EventDispatcher.install(scope: CoroutineScope, configuration: Config.() -> Unit): Market {
            return Market(Config().apply(configuration)).also { feature ->

            }
        }
    }
}
