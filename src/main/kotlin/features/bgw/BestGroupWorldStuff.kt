package xyz.swagbot.features.bgw

import discord4j.core.*
import discord4j.core.event.domain.message.*
import io.facet.core.*
import io.facet.discord.*
import io.facet.discord.commands.*
import io.facet.discord.commands.events.*
import io.facet.discord.extensions.*
import org.joda.time.*
import xyz.swagbot.features.*

class BestGroupWorldStuff private constructor() {

    companion object : DiscordClientFeature<EmptyConfig, BestGroupWorldStuff>("bgw") {

        override fun install(client: DiscordClient, configuration: EmptyConfig.() -> Unit): BestGroupWorldStuff {
            client.listen<MessageCreateEvent>()
                .filterWhen { event -> event.message.channel.map { it.id.asLong() == 402224449367179264 } }
                .filter { it.message.attachments.isNotEmpty() || it.message.embeds.isNotEmpty() }
                .flatMap { it.message.delete("No images in #chat.") }
                .subscribe()

            client.listen<MessageCreateEvent>()
                .filter { event -> event.message.author.map { !it.isBot }.orElse(false) }
                .filter { event -> event.guildId.map { it.asLong() == 97342233241464832 }.orElse(false)  }
                .filter { event -> event.message.content.map { it.toLowerCase().contains("sir") }.orElse(false) }
                .flatMap { event ->
                    event.message.channel.flatMap { channel ->
                        channel.createMessage("**${event.member.get().mention} I'LL SHOW YOU SIR**")
                    }
                }
                .subscribe()

//            client.listen<MessageCreateEvent>()
//                .filter { event -> event.message.author.map { !it.isBot }.orElse(false) }
//                .filterWhen { event -> event.message.channel.map { it.id.asLong() == 214225348185817091 } }
//                .flatMap { event ->
//                    val dispatcher = event.client.feature(ChatCommands).dispatcher
//                    event.message.channel.flatMap {
//                        it.createMessage(
//                            dispatcher.getAllUsage(dispatcher.root, ChatCommandSource(event, "", ""), false)
//                                .reduce { acc, s -> acc + "$s\n" }
//                        )
//                    }
//                }
//                .subscribe()

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

            return BestGroupWorldStuff()
        }
    }
}
