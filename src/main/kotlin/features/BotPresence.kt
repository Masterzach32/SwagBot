package xyz.swagbot.features

import discord4j.core.*
import discord4j.core.`object`.presence.*
import discord4j.core.event.domain.lifecycle.*
import io.facet.discord.*
import io.facet.discord.extensions.*
import xyz.swagbot.features.music.*

class BotPresence(config: Config) {

    class Config

    companion object : DiscordClientFeature<Config, BotPresence>("presence", listOf(Music)) {

        override fun install(client: DiscordClient, configuration: Config.() -> Unit): BotPresence {
            return BotPresence(Config().apply(configuration)).also { feature ->
                client.listen<ReadyEvent>()
                    .flatMap { client.updatePresence(Presence.online(Activity.listening("~help"))) }
                    .subscribe()
            }
        }
    }
}
