package xyz.swagbot.features.music.commands

import com.mojang.brigadier.*
import com.mojang.brigadier.arguments.StringArgumentType.*
import com.sedmelluq.discord.lavaplayer.track.*
import discord4j.core.`object`.entity.*
import discord4j.core.`object`.reaction.*
import discord4j.core.event.domain.message.*
import io.facet.discord.commands.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*
import reactor.core.publisher.*
import xyz.swagbot.features.music.*
import xyz.swagbot.util.*
import java.time.*

object YouTubeSearch : ChatCommand {

    private val emojiUnicode = listOf("\u0031\u20e3", "\u0032\u20e3", "\u0033\u20e3", "\u0034\u20e3",
        "\u0035\u20e3", "\u0036\u20e3", "\u0037\u20e3", "\u0038\u20e3", "\u0039\u20e3", "\u0030\u20e3")

    override fun register(dispatcher: CommandDispatcher<ChatCommandSource>) {
        listOf("search", "s", "ytsearch").map { alias ->
            literal(alias).then(argument("query", greedyString()).executes { context ->
                if (!context.source.member.isPresent)
                    return@executes 1
                val requester = context.source.member.get()
                val feature = context.source.client.feature(Music)
                val searchCount = 5
                context.source.channel.flatMap { it.type() }.subscribe()
                feature.search("ytsearch:${context.getString("query")}", searchCount) { results ->
                    context.source.channel.flatMap { channel ->
                        if (results.isEmpty())
                            channel.respondError()
                        else
                            channel.respondSuccess(results, requester).flatMap { message ->
                                emojiUnicode.toFlux().take(searchCount.toLong())
                                    .flatMap { message.addReaction(ReactionEmoji.unicode(it)) }
                                    .then()
                                    .switchIfEmpty {
                                        context.source.client.listen<ReactionAddEvent>()
                                            .filter { event ->
                                                event.userId == requester.id &&
                                                        event.messageId == message.id &&
                                                        event.emoji.asUnicodeEmoji()
                                                            .map { emojiUnicode.contains(it.raw) }
                                                            .orElse(false)
                                            }
                                            .next()
                                            .flatMap { event ->
                                                event.message.flatMap { it.removeAllReactions() }.switchIfEmpty {
                                                    val index = emojiUnicode
                                                        .indexOf(event.emoji.asUnicodeEmoji().get().raw)
                                                    val track = results[index].also { track ->
                                                        track.userData = TrackContext(requester.id, channel.id)
                                                        feature
                                                            .trackSchedulerFor(context.source.guildId.get())
                                                            .queue(track)
                                                    }
                                                    channel
                                                        .createEmbed(
                                                            trackRequestedTemplate(requester.displayName, track)
                                                        )
                                                        .then()
                                                }
                                            }.timeout(
                                                Duration.ofSeconds(60),
                                                Mono.just(message).flatMap { it.removeAllReactions() }
                                            )
                                    }
                            }
                    }.subscribe()
                }.let { 1 }
            })
        }.forEach { dispatcher.register(it) }
    }

    private fun MessageChannel.respondError() = createEmbed(
        errorTemplate.andThen {
            it.setDescription("Sorry, I could not find a video that matched that description. Try refining your search.")
        }
    )

    private fun MessageChannel.respondSuccess(results: List<AudioTrack>, requester: Member) = createEmbed(
        baseTemplate.andThen { spec ->
            spec.setTitle("YouTube Search Result")
            val list = results
                .map { it.info }
                .mapIndexed { i, track ->
                    "${i+1}. [**${track.title}** by **${track.author}**](${track.uri})\n"
                }
                .reduce { acc, s -> acc + s }

            spec.setDescription(
                "$list\n${requester.mention}, if you would like to queue one of these videos, select it's " +
                        "corresponding reaction below within 60 seconds."
            )
        }
    )
}
