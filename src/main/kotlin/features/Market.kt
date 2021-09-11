package xyz.swagbot.features

import discord4j.core.event.EventDispatcher
import io.facet.core.EventDispatcherFeature
import kotlinx.coroutines.CoroutineScope
import xyz.swagbot.features.guilds.GuildStorage

class Market(config: Config) {

    class Config

    companion object : EventDispatcherFeature<Config, Market>("market", listOf(GuildStorage)) {

        override suspend fun EventDispatcher.install(scope: CoroutineScope, configuration: Config.() -> Unit): Market {
            return Market(Config().apply(configuration)).also { feature ->

            }
        }
    }
}
