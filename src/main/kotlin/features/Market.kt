package xyz.swagbot.features

import discord4j.core.*
import io.facet.discord.*
import xyz.swagbot.features.guilds.*

class Market(config: Config) {

    class Config

    companion object : DiscordClientFeature<Config, Market>("market", listOf(GuildStorage)) {

        override fun install(client: DiscordClient, configuration: Config.() -> Unit): Market {
            return Market(Config().apply(configuration)).also { feature ->

            }
        }
    }
}
