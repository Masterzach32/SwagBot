package xyz.swagbot.features.music.commands

import com.mojang.brigadier.arguments.StringArgumentType.*
import com.mojang.brigadier.builder.*
import discord4j.core.*
import io.facet.discord.commands.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*
import reactor.core.publisher.*
import xyz.swagbot.extensions.*
import xyz.swagbot.features.music.*
import xyz.swagbot.util.*

object PlayCommand : ChatCommand(
    name = "Play Track",
    aliases = setOf("play", "p"),
    scope = Scope.GUILD,
    category = "music"
) {

    override fun register(client: DiscordClient, node: LiteralArgumentBuilder<ChatCommandSource>) {
        node.then(argument("url", greedyString()).executesAsync { context ->
            val source = context.source
            source.handlePremium().switchIfEmpty {
                source.channel.flatMap { channel ->
                    source.member.map { member ->
                        member.voiceState.flatMap { _ ->
                            val query = context.getString("url").let { query ->
                                if ("http://" !in query && "https://" !in query)
                                    "ytsearch:$query"
                                else
                                    query
                            }
                            val feature = source.client.feature(Music)
                            val scheduler = feature.trackSchedulerFor(source.guildId.get())

                            source.message.channel.flatMap { it.type() }.then(
                                feature.search(query)
                                    .doOnNext { it.setTrackContext(member, channel) }
                                    .doOnNext { scheduler.queue(it) }
                                    .flatMap {
                                        channel.createEmbed(
                                            trackRequestedTemplate(member.displayName, it, scheduler.queueTimeLeft())
                                        )
                                    }
                            )
                        }.switchIfEmpty {
                            channel.createEmbed(errorTemplate.andThen {
                                it.setDescription(
                                    "You must be connected to a voice channel before you can use this command!"
                                )
                            })
                        }
                    }.get()
                }.then()
            }
        })
    }
}
