package xyz.swagbot.features.bgw

import discord4j.core.*
import discord4j.core.event.domain.message.*
import io.facet.discord.*
import io.facet.discord.extensions.*
import org.joda.time.*
import xyz.swagbot.features.*

class BestGroupWorldStuff private constructor() {

    companion object : DiscordClientFeature<EmptyConfig, BestGroupWorldStuff>("bgw") {

        override fun install(client: DiscordClient, configuration: EmptyConfig.() -> Unit): BestGroupWorldStuff {
            client.listen<MessageCreateEvent>()
                .filterWhen { event -> event.message.channel.map { it.id.asLong() == 402224449367179264L } }
                .filter { it.message.attachments.isNotEmpty() || it.message.embeds.isNotEmpty() }
                .flatMap { it.message.delete("No images in #chat.") }
                .subscribe()

//            client.listen<MessageCreateEvent>()
//                .filterWhen { event -> event.guild.map { it.id.asLong() == 97342233241464832L } }
//                .filter { event -> event.member.map { it.id.asLong() == 141675140940300288L }.orElse(false) }
//                .take(3)
//                .flatMap { event ->
//                    event.message.channel.flatMap { channel ->
//                        channel.createMessage("${event.member.get().mention} go to bed!")
//                    }
//                }
//                .subscribe()

            return BestGroupWorldStuff().also { feature ->

            }
        }
    }
}
