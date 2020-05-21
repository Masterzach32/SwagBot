package xyz.swagbot.features.music.commands

import com.mojang.brigadier.arguments.StringArgumentType.*
import com.mojang.brigadier.builder.*
import com.sedmelluq.discord.lavaplayer.track.*
import discord4j.core.*
import discord4j.core.`object`.entity.*
import discord4j.core.`object`.reaction.*
import discord4j.core.event.domain.message.*
import io.facet.discord.commands.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactive.*
import kotlinx.coroutines.reactor.*
import reactor.core.publisher.*
import xyz.swagbot.extensions.*
import xyz.swagbot.features.music.*
import xyz.swagbot.util.*
import java.time.*

object YouTubeSearch : ChatCommand(
    name = "YouTube Search",
    aliases = setOf("search", "s", "ytsearch"),
    scope = Scope.GUILD,
    category = "music"
) {

    private val emojiUnicode = listOf("\u0031\u20e3", "\u0032\u20e3", "\u0033\u20e3", "\u0034\u20e3",
        "\u0035\u20e3", "\u0036\u20e3", "\u0037\u20e3", "\u0038\u20e3", "\u0039\u20e3", "\u0030\u20e3")

    override fun register(client: DiscordClient, node: LiteralArgumentBuilder<ChatCommandSource>) {
        node.then(argument("query", greedyString()).executesSuspend { context ->
            val requester = context.source.member.get()
            val feature = context.source.client.feature(Music)
            val searchCount = 5

            val channel = context.source.channel.await()
            if (!context.source.isMusicFeatureEnabled()) {
                channel.createEmbed(errorTemplate.andThen {
                    it.setDescription("Music commands are a premium feature of SwagBot. Type " +
                            "`${context.source.prefixUsed}premium` to learn more.")
                })
                return@executesSuspend
            }

            context.source.channel.await().type().await()

            val results = try {
                feature.search("ytsearch:${context.getString("query")}", Music.SearchResultPolicy.Limited(searchCount))
            } catch (e: Throwable) {
                channel.createEmbed(errorTemplate.andThen {
                    it.setDescription("Oops! Something went wrong when trying to search youtube.")
                }).await()
                return@executesSuspend
            }

            if (results.isEmpty()) {
                channel.createEmbed(errorTemplate.andThen {
                    it.setDescription("Sorry, I could not find any videos that matched that description. " +
                            "Try refining your search.")
                }).await()
                return@executesSuspend
            }

            val message = context.source.message
            emojiUnicode.asFlow()
                .take(searchCount)
                .map { message.addReaction(ReactionEmoji.unicode(it)).await() }
                .collect()

            context.source.client.listen<ReactionAddEvent>()
                .filter { event ->
                    event.userId == requester.id && event.messageId == message.id &&
                            event.emoji.asUnicodeEmoji().map { emojiUnicode.contains(it.raw) }.orElse(false)
                }
                .take(1)
                .timeout(Duration.ofSeconds(60))
                .asFlow()
                .onEach { event ->
                    event.message.await().removeAllReactions().await()

                    val trackScheduler = feature.trackSchedulerFor(context.source.guildId.get())
                    val index = emojiUnicode.indexOf(event.emoji.asUnicodeEmoji().get().raw)
                    val track = results[index].also { track ->
                        track.userData = TrackContext(requester.id, channel.id)
                        trackScheduler.queue(track)
                    }
                    channel.createEmbed(
                        trackRequestedTemplate(requester.displayName, track, trackScheduler.queueTimeLeft())
                    ).await()
                }
                .collect()

//            context.source.handlePremium().switchIfEmpty {
//                context.source.channel.flatMap { it.type() }.switchIfEmpty {
//                    context.source.channel.flatMap { channel ->
//                        feature.search("ytsearch:${context.getString("query")}", searchCount).flatMap { results ->
//                            channel.respondSuccess(results, requester).flatMap { message ->
//                                emojiUnicode.toFlux().take(searchCount.toLong())
//                                    .flatMap { message.addReaction(ReactionEmoji.unicode(it)) }
//                                    .then(context.source.client.listen<ReactionAddEvent>()
//                                        .filter { event ->
//                                            event.userId == requester.id &&
//                                                    event.messageId == message.id &&
//                                                    event.emoji.asUnicodeEmoji()
//                                                        .map { emojiUnicode.contains(it.raw) }
//                                                        .orElse(false)
//                                        }
//                                        .next()
//                                        .flatMap { event ->
//                                            event.message.flatMap { it.removeAllReactions() }.switchIfEmpty {
//                                                val trackScheduler = feature.trackSchedulerFor(context.source.guildId.get())
//                                                val index = emojiUnicode
//                                                    .indexOf(event.emoji.asUnicodeEmoji().get().raw)
//                                                val track = results[index].also { track ->
//                                                    track.userData = TrackContext(requester.id, channel.id)
//                                                    trackScheduler.queue(track)
//                                                }
//                                                channel
//                                                    .createEmbed(
//                                                        trackRequestedTemplate(
//                                                            requester.displayName,
//                                                            track,
//                                                            trackScheduler.queueTimeLeft())
//                                                    )
//                                                    .then()
//                                            }
//                                        }.timeout(
//                                            Duration.ofSeconds(60),
//                                            message.toMono().flatMap { it.removeAllReactions() }
//                                        )
//                                    )
//                            }
//                        }.onErrorResume {
//                            it.printStackTrace()
//                            channel.respondError().then()
//                        }
//                    }
//                }
//            }
        })
    }

//    private fun MessageChannel.respondError() = createEmbed(
//        errorTemplate.andThen {
//            it.setDescription("Sorry, I could not find a video that matched that description. Try refining your search.")
//        }
//    )
//
//    private fun MessageChannel.respondSuccess(results: List<AudioTrack>, requester: Member) = createEmbed(
//        baseTemplate.andThen { spec ->
//            spec.setTitle("YouTube Search Result")
//            val list = results
//                .map { it.info }
//                .mapIndexed { i, track ->
//                    "${i+1}. [**${track.title}** by **${track.author}**](${track.uri})\n"
//                }
//                .reduce { acc, s -> acc + s }
//
//            spec.setDescription(
//                "$list\n${requester.mention}, if you would like to queue one of these videos, select it's " +
//                        "corresponding reaction below within 60 seconds."
//            )
//        }
//    )
}
