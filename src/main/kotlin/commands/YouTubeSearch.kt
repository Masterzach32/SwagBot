package xyz.swagbot.commands

import discord4j.core.`object`.component.*
import discord4j.core.event.domain.interaction.*
import discord4j.core.event.domain.message.*
import discord4j.discordjson.json.*
import discord4j.rest.util.*
import io.facet.core.extensions.*
import io.facet.discord.appcommands.*
import io.facet.discord.appcommands.extensions.*
import io.facet.discord.extensions.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import xyz.swagbot.extensions.*
import xyz.swagbot.features.music.*
import xyz.swagbot.util.*
import kotlin.time.*

@OptIn(ExperimentalTime::class, DelicateCoroutinesApi::class)
object YouTubeSearch : GlobalGuildApplicationCommand {

    private val emojiUnicode = listOf(
        "\u0031\u20e3", "\u0032\u20e3", "\u0033\u20e3", "\u0034\u20e3",
        "\u0035\u20e3", "\u0036\u20e3", "\u0037\u20e3", "\u0038\u20e3", "\u0039\u20e3", "\u0030\u20e3"
    )

    private val resultsWorkerScope = GlobalScope

    override val request =
        applicationCommandRequest("search", "Search YouTube and select a video to play using reaction buttons.") {
            addOption("query", "The search term to look up on YouTube.", ApplicationCommandOptionType.STRING, true)
            addOption("count", "The number of results to show.", ApplicationCommandOptionType.INTEGER, false) {
                addChoice("Five results", 5)
                addChoice("Ten results", 10)
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
            return event.replyEphemeral("Music is a premium feature of SwagBot").await()
        }

        acknowledge()

        val results = try {
            music.search(
                "ytsearch:$query",
                Music.SearchResultPolicy.Limited(count)
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

        val components: List<ComponentData> =
            (1..count)
                .chunked(5)
                .map { row -> row.map { Button.primary("$it", "$it") } }
                .map { ActionRow.of(it).data } + ActionRow.of(Button.secondary("cancel", "Cancel")).data

        val resultsMessageRequest: MultipartRequest<WebhookExecuteRequest> =
            MultipartRequest.ofRequest(WebhookExecuteRequest.builder().addEmbed(baseTemplate.andThen {
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

            client.flowOf<ButtonInteractEvent>()
                .filter { it.interaction.message.unwrap()?.id == resultMessage.id }
                .take(1)
                .onEach { buttonEvent -> buttonEvent.edit { it.setComponents(emptyList()) }.await() }
                .filter { it.customId != "cancel" }
                .collect { buttonEvent ->
                    buttonEvent.edit { it.setComponents(emptyList()) }.await()
                    val choice = buttonEvent.customId.toInt()
                    val trackScheduler = music.trackSchedulerFor(guildId)
                    val track = results[choice-1].also { track ->
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
                        member.getConnectedVoiceChannel()?.join()
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
