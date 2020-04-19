package xyz.swagbot.features.music.commands

import com.mojang.brigadier.*
import com.mojang.brigadier.arguments.StringArgumentType.*
import io.facet.discord.commands.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*
import reactor.core.publisher.*
import xyz.swagbot.features.music.*
import xyz.swagbot.util.*

object PlayCommand : ChatCommand {

    override fun register(dispatcher: CommandDispatcher<ChatCommandSource>) {
        listOf("play", "p").map { alias ->
            literal(alias).then(argument("url", greedyString()).executes { context ->
                val source = context.source
                source.guildId.ifPresent { guildId ->
                    source.channel.flatMap { channel ->
                        source.member.map { member ->
                            member.voiceState.flatMap {
                                val query = context.getString("url").let { query ->
                                    if ("http://" !in query && "https://" !in query)
                                        "ytsearch:$query"
                                    else
                                        query
                                }
                                val feature = source.client.feature(Music)
                                val trackScheduler = feature.trackSchedulerFor(guildId)

                                feature.audioPlayerManager.loadItemOrdered(
                                    trackScheduler,
                                    query,
                                    AudioTrackLoadHandler(
                                        trackScheduler,
                                        member,
                                        channel
                                    )
                                )
                                source.message.channel.flatMap { it.type() }
                            }.switchIfEmpty {
                                channel.createEmbed(errorTemplate.andThen {
                                    it.setDescription(
                                        "You must be connected to a voice channel before you can use this command!"
                                    )
                                }).then()
                            }
                        }.get()
                    }.subscribe()

                }.let { 1 }
            })
        }.forEach { dispatcher.register(it) }
    }
}
