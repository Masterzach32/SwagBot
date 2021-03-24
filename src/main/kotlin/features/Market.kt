package xyz.swagbot.features

import discord4j.core.event.*
import io.facet.discord.*
import xyz.swagbot.features.guilds.*

class Market(config: Config) {

    class Config

    companion object : EventDispatcherFeature<Config, Market>("market", listOf(GuildStorage)) {

        override fun install(dispatcher: EventDispatcher, configuration: Config.() -> Unit): Market {
            return Market(Config().apply(configuration)).also { feature ->

            }
        }
    }
}
