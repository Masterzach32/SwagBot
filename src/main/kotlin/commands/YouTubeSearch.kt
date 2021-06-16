package xyz.swagbot.commands

import discord4j.core.`object`.reaction.*
import discord4j.core.event.domain.message.*
import discord4j.rest.util.*
import io.facet.core.extensions.*
import io.facet.discord.appcommands.*
import io.facet.discord.appcommands.extensions.*
import io.facet.discord.extensions.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactive.*
import xyz.swagbot.extensions.*
import xyz.swagbot.features.music.*
import xyz.swagbot.util.*
import java.time.*
import java.util.concurrent.*

object YouTubeSearch : GlobalGuildApplicationCommand {

    private val emojiUnicode = listOf("\u0031\u20e3", "\u0032\u20e3", "\u0033\u20e3", "\u0034\u20e3",
        "\u0035\u20e3", "\u0036\u20e3", "\u0037\u20e3", "\u0038\u20e3", "\u0039\u20e3", "\u0030\u20e3")

    override val request = applicationCommandRequest("search", "Search YouTube and select a video to play using reaction buttons.") {
        addOption("query", "The search term to look up on YouTube.", ApplicationCommandOptionType.STRING, true)
        addOption("count", "The number of results to show.", ApplicationCommandOptionType.INTEGER, false) {
            addChoice("Five results", 5)
            addChoice("Ten results", 10)
        }
    }

    override suspend fun GuildInteractionContext.execute() {
        val music = client.feature(Music)
        val searchCount = command.getOption("count").unwrap()?.value?.unwrap()?.asLong()?.toInt() ?: 5

        val channel = getChannel()
        val guild = getGuild()
        if (!guild.isPremium()) {
            return event.replyEphemeral("Music is a premium feature of SwagBot").await()
        }

        acknowledge()

        val results = try {
            music.search(
                "ytsearch:${command.getOption("query").get().value.get().asString()}",
                Music.SearchResultPolicy.Limited(searchCount)
            )
        } catch (e: Throwable) {
            createFollowupMessage(errorTemplate("Oops! Something went wrong when trying to search youtube.", e))
            return
        }

        if (results.isEmpty()) {
            event.interactionResponse.createFollowupMessage(
                "Sorry, I could not find any videos that matched that description. Try refining your search."
            ).await()
            return
        }

        val resultMessage = createFollowupMessage(
            baseTemplate.andThen {
                title = "YouTube Search Result"
                val list = results
                    .mapIndexed { i, track -> "${i + 1}. ${track.info.boldFormattedTitleWithLink}" }
                    .joinToString(separator = "\n")

                description = "$list\nIf you would like to queue one of these videos, select it's " +
                    "corresponding reaction below within 60 seconds."
            }
        ).let {
            client.getMessageById(channel.id, it.id().asString().toSnowflake()).await()
        }

        emojiUnicode
            .take(searchCount)
            .forEach { resultMessage.addReaction(ReactionEmoji.unicode(it)).await() }

        GlobalScope.launch {
            try {
                val event = client.on<ReactionAddEvent>()
                    .filter { event ->
                        event.userId == member.id && event.messageId == resultMessage.id &&
                            event.emoji.asUnicodeEmoji().map { emojiUnicode.contains(it.raw) }.orElse(false)
                    }
                    .timeout(Duration.ofSeconds(60))
                    .asFlow()
                    .first()

                val trackScheduler = music.trackSchedulerFor(guildId)
                val index = emojiUnicode.indexOf(event.emoji.asUnicodeEmoji().get().raw)
                val track = results[index].also { track ->
                    track.setTrackContext(member, channel)
                    trackScheduler.queue(track)
                }
                resultMessage.edit {
                    it.setEmbed(trackRequestedTemplate(member.displayName, track, trackScheduler.queueTimeLeft))
                }.await()

                if (getGuild().getConnectedVoiceChannel() == null)
                    member.getConnectedVoiceChannel()?.join()
            } catch (e: TimeoutException) {

            } finally {
                resultMessage.removeAllReactions().await()
            }
        }
    }
}
