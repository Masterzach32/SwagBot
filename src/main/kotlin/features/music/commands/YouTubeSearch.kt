package xyz.swagbot.features.music.commands

import com.mojang.brigadier.arguments.StringArgumentType.*
import discord4j.core.`object`.reaction.*
import discord4j.core.event.domain.message.*
import io.facet.discord.commands.*
import io.facet.discord.commands.dsl.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*
import kotlinx.coroutines.*
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
    category = "music",
    description = "Search YouTube and select a video to play using reaction buttons.",
    usage = commandUsage {
        add(
            "<query>",
            "Will search YouTube and list the first five results found. Click on the " +
                    "corresponding reaction to queue that video."
        )
    }
) {

    private val emojiUnicode = listOf("\u0031\u20e3", "\u0032\u20e3", "\u0033\u20e3", "\u0034\u20e3",
        "\u0035\u20e3", "\u0036\u20e3", "\u0037\u20e3", "\u0038\u20e3", "\u0039\u20e3", "\u0030\u20e3")

    override fun DSLCommandNode<ChatCommandSource>.register() {
        argument("query", greedyString()) {
            runs { context ->
                val feature = client.feature(Music)
                val searchCount = 5

                val channel = getGuildChannel()
                if (!isMusicFeatureEnabled()) {
                    channel.sendEmbed(notPremiumTemplate(prefixUsed))
                    return@runs
                }

                launch { channel.type().await() }

                val results = try {
                    feature.search("ytsearch:${context.getString("query")}", Music.SearchResultPolicy.Limited(searchCount))
                } catch (e: Throwable) {
                    channel.sendEmbed(errorTemplate.andThen {
                        description = "Oops! Something went wrong when trying to search youtube."
                    })
                    return@runs
                }

                if (results.isEmpty()) {
                    channel.sendEmbed(errorTemplate.andThen {
                        description = "Sorry, I could not find any videos that matched that description. " +
                                "Try refining your search."
                    })
                    return@runs
                }

                val resultMessage = channel.sendEmbed(
                    baseTemplate.andThen {
                        title = "YouTube Search Result"
                        val list = results
                            .mapIndexed { i, track -> "${i + 1}. ${track.info.boldFormattedTitleWithLink}" }
                            .joinToString(separator = "\n")

                        description = "$list\n${member.mention}, if you would like to queue one of these videos, " +
                                "select it's corresponding reaction below within 60 seconds."
                    }
                )

                emojiUnicode
                    .take(searchCount)
                    .forEach { resultMessage.addReaction(ReactionEmoji.unicode(it)).await() }

                val flow = client.on<ReactionAddEvent>()
                    .filter { event ->
                        event.userId == member.id && event.messageId == resultMessage.id &&
                                event.emoji.asUnicodeEmoji().map { emojiUnicode.contains(it.raw) }.orElse(false)
                    }
                    .take(1)
                    .timeout(Duration.ofSeconds(60))
                    .asFlow()

                launch {
                    try {
                        val event = flow.first()

                        resultMessage.delete().await()

                        val trackScheduler = feature.trackSchedulerFor(guildId!!)
                        val index = emojiUnicode.indexOf(event.emoji.asUnicodeEmoji().get().raw)
                        val track = results[index].also { track ->
                            track.userData = TrackContext(member.id, channel.id)
                            trackScheduler.queue(track)
                        }
                        channel.sendEmbed(trackRequestedTemplate(member.displayName, track, trackScheduler.queueTimeLeft()))
                    } catch (e: TimeoutException) {
                        resultMessage.removeAllReactions().await()
                    }
                }
            }
        }
    }
}
