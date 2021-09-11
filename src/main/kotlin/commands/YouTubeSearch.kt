package xyz.swagbot.commands

import discord4j.core.`object`.component.ActionRow
import discord4j.core.`object`.component.Button
import discord4j.core.event.domain.message.MessageDeleteEvent
import discord4j.discordjson.json.ComponentData
import discord4j.discordjson.json.WebhookExecuteRequest
import discord4j.rest.util.ApplicationCommandOptionType
import discord4j.rest.util.MultipartRequest
import io.facet.commands.GlobalGuildApplicationCommand
import io.facet.commands.GuildSlashCommandContext
import io.facet.commands.acknowledge
import io.facet.commands.applicationCommandRequest
import io.facet.common.*
import io.facet.common.dsl.and
import io.facet.core.feature
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import xyz.swagbot.extensions.boldFormattedTitleWithLink
import xyz.swagbot.extensions.isPremium
import xyz.swagbot.extensions.joinWithAutoDisconnect
import xyz.swagbot.extensions.setTrackContext
import xyz.swagbot.features.music.Music
import xyz.swagbot.features.music.trackRequestedTemplate
import xyz.swagbot.util.baseTemplate
import xyz.swagbot.util.errorTemplate
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class, DelicateCoroutinesApi::class)
object YouTubeSearch : GlobalGuildApplicationCommand {

    private val emojiUnicode = listOf(
        "\u0031\u20e3", "\u0032\u20e3", "\u0033\u20e3", "\u0034\u20e3",
        "\u0035\u20e3", "\u0036\u20e3", "\u0037\u20e3", "\u0038\u20e3", "\u0039\u20e3", "\u0030\u20e3"
    )

    private val resultsWorkerScope = GlobalScope

    override val request =
        applicationCommandRequest("search", "Search YouTube and select a video to play using reaction buttons.") {
            string("query", "The search term to look up on YouTube.", true)
            option("count", "The number of results to show.", ApplicationCommandOptionType.INTEGER, false) {
                choice("Five results", 5)
                choice("Ten results", 10)
            }
        }

    override suspend fun GuildSlashCommandContext.execute() {
        val music = client.feature(Music)
        val query: String by options
        val type: String? by options.nullable()
        val count: Int by options.defaultValue(5)

        val channel = getChannel()
        val guild = getGuild()
        if (!guild.isPremium()) {
            return event.reply("Music is a premium feature of SwagBot").withEphemeral(true).await()
        }

        acknowledge()

        val results = try {
            music.search(
                "ytsearch:$query",
                Music.SearchResultPolicy.Limited(count)
            )
        } catch (e: Throwable) {
            interactionResponse.sendFollowupMessage(
                errorTemplate("Oops! Something went wrong when trying to search youtube.", e)
            )
            return
        }

        if (results.isEmpty()) {
            interactionResponse.sendFollowupMessage(
                "Sorry, I could not find any videos that matched that description. Try refining your search."
            )
            return
        }

        val components: List<ComponentData> =
            (1..count)
                .chunked(5)
                .map { row -> row.map { Button.primary("$it", "$it") } }
                .map { ActionRow.of(it).data } + ActionRow.of(Button.secondary("cancel", "Cancel")).data

        val resultsMessageRequest: MultipartRequest<WebhookExecuteRequest> =
            MultipartRequest.ofRequest(WebhookExecuteRequest.builder().addEmbed(baseTemplate.and {
                title = "YouTube Search Result"
                val list = results
                    .mapIndexed { i, track -> "${i + 1}. ${track.info.boldFormattedTitleWithLink}" }
                    .joinToString(separator = "\n")

                description = "$list\nIf you would like to queue one of these videos, select it's " +
                    "corresponding button below."
            }.asRequest()).components(components).build())

        val resultMessage = event.interactionResponse.createFollowupMessage(resultsMessageRequest).await().let {
            client.getMessageById(channel.id, it.id().asString().toSnowflake()).await()
        }

        resultsWorkerScope.launch {
            client.flowOf<MessageDeleteEvent>()
                .filter { it.messageId == resultMessage.id }
                .onEach { cancel("Search results message was deleted.") }
                .launchIn(this)

            val buttonEvents = resultMessage.buttonEvents

            buttonEvents
                .filter { it.interaction.user.id != user.id }
                .onEach { buttonEvent ->
                    buttonEvent.reply("You cannot choose a track on someone else's interaction!")
                        .withEphemeral(true)
                        .await()
                }
                .launchIn(this)

            buttonEvents
                .filter { it.interaction.user.id == user.id }
                .take(1)
                .onEach { buttonEvent -> buttonEvent.edit().withComponents(emptyList()).await() }
                .filter { it.customId != "cancel" }
                .collect { buttonEvent ->
                    val choice = buttonEvent.customId.toInt()
                    val trackScheduler = music.trackSchedulerFor(guildId)
                    val track = results[choice - 1].also { track ->
                        track.setTrackContext(member, channel)
                        trackScheduler.queue(track)
                    }
                    val request: MultipartRequest<WebhookExecuteRequest> = MultipartRequest.ofRequest(
                        WebhookExecuteRequest.builder().addEmbed(
                            trackRequestedTemplate(
                                member.displayName,
                                track,
                                trackScheduler.queueTimeLeft
                            ).asRequest()
                        ).build()
                    )
                    buttonEvent.interactionResponse.createFollowupMessage(request).await()

                    if (getGuild().getConnectedVoiceChannel() == null)
                        member.getConnectedVoiceChannel()?.joinWithAutoDisconnect()
                    cancel("Search results ${resultMessage.id} completed successfully")
                }
        }

//        emojiUnicode
//            .take(searchCount)
//            .forEach { resultMessage.addReaction(ReactionEmoji.unicode(it)).await() }
//
//        GlobalScope.launch {
//            try {
//                val event = client.on<ReactionAddEvent>()
//                    .filter { event ->
//                        event.userId == member.id && event.messageId == resultMessage.id &&
//                            event.emoji.asUnicodeEmoji().map { emojiUnicode.contains(it.raw) }.orElse(false)
//                    }
//                    .timeout(Duration.ofSeconds(60))
//                    .asFlow()
//                    .first()
//
//                val trackScheduler = music.trackSchedulerFor(guildId)
//                val index = emojiUnicode.indexOf(event.emoji.asUnicodeEmoji().get().raw)
//                val track = results[index].also { track ->
//                    track.setTrackContext(member, channel)
//                    trackScheduler.queue(track)
//                }
//                resultMessage.edit {
//                    it.setEmbed(trackRequestedTemplate(member.displayName, track, trackScheduler.queueTimeLeft))
//                }.await()
//
//                if (getGuild().getConnectedVoiceChannel() == null)
//                    member.getConnectedVoiceChannel()?.join()
//            } catch (e: TimeoutException) {
//
//            } finally {
//                resultMessage.removeAllReactions().await()
//            }
//        }
    }
}
