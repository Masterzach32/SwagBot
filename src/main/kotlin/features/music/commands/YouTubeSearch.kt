package xyz.swagbot.features.music.commands

import com.mojang.brigadier.arguments.StringArgumentType.*
import discord4j.core.*
import discord4j.core.`object`.reaction.*
import discord4j.core.event.domain.message.*
import io.facet.discord.commands.*
import io.facet.discord.commands.dsl.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactive.*
import xyz.swagbot.extensions.*
import xyz.swagbot.features.music.*
import xyz.swagbot.util.*
import java.time.*
import java.util.concurrent.*

object YouTubeSearch : ChatCommand(
    name = "YouTube Search",
    aliases = setOf("search", "s", "ytsearch"),
    scope = Scope.GUILD,
    category = "music"
) {

    private val emojiUnicode = listOf("\u0031\u20e3", "\u0032\u20e3", "\u0033\u20e3", "\u0034\u20e3",
        "\u0035\u20e3", "\u0036\u20e3", "\u0037\u20e3", "\u0038\u20e3", "\u0039\u20e3", "\u0030\u20e3")

    override fun DSLCommandNode<ChatCommandSource>.register(client: GatewayDiscordClient) {
        argument("query", greedyString()) {
            runs { context ->
                val requester = member!!
                val feature = client.feature(Music)
                val searchCount = 5

                val channel = getChannel()
                if (!isMusicFeatureEnabled())
                    return@runs channel.createEmbed(notPremiumTemplate(prefixUsed)).awaitComplete()

                channel.type().async()

                val results = try {
                    feature.search("ytsearch:${context.getString("query")}", Music.SearchResultPolicy.Limited(searchCount))
                } catch (e: Throwable) {
                    return@runs channel.createEmbed(errorTemplate.andThen {
                        it.setDescription("Oops! Something went wrong when trying to search youtube.")
                    }).awaitComplete()
                }

                if (results.isEmpty()) {
                    return@runs channel.createEmbed(errorTemplate.andThen {
                        it.setDescription("Sorry, I could not find any videos that matched that description. " +
                                "Try refining your search.")
                    }).awaitComplete()
                }

                val resultMessage = channel.createEmbed(
                    baseTemplate.andThen { spec ->
                        spec.setTitle("YouTube Search Result")
                        val list = results
                            .mapIndexed { i, track -> "${i + 1}. ${track.info.boldFormattedTitleWithLink}" }
                            .joinToString(separator = "\n")

                        spec.setDescription(
                            "$list\n${requester.mention}, if you would like to queue one of these videos, select " +
                                    "it's corresponding reaction below within 60 seconds."
                        )
                    }
                ).await()

                emojiUnicode.asFlow()
                    .take(searchCount)
                    .map { resultMessage.addReaction(ReactionEmoji.unicode(it)).await() }
                    .collect()

                val flow = client.on<ReactionAddEvent>()
                    .filter { event ->
                        event.userId == requester.id && event.messageId == resultMessage.id &&
                                event.emoji.asUnicodeEmoji().map { emojiUnicode.contains(it.raw) }.orElse(false)
                    }
                    .take(1)
                    .timeout(Duration.ofSeconds(60))
                    .asFlow()

                try {
                    val event = flow.first()

                    resultMessage.delete().async()

                    val trackScheduler = feature.trackSchedulerFor(guildId!!)
                    val index = emojiUnicode.indexOf(event.emoji.asUnicodeEmoji().get().raw)
                    val track = results[index].also { track ->
                        track.userData = TrackContext(requester.id, channel.id)
                        trackScheduler.queue(track)
                    }
                    channel.createEmbed(
                        trackRequestedTemplate(requester.displayName, track, trackScheduler.queueTimeLeft())
                    ).awaitComplete()
                } catch (e: TimeoutException) {
                    resultMessage.removeAllReactions().await()
                }
            }
        }
    }
}
