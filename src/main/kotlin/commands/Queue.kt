package xyz.swagbot.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.reaction.ReactionEmoji
import discord4j.core.event.domain.message.ReactionAddEvent
import io.facet.chatcommands.*
import io.facet.common.await
import io.facet.common.awaitNullable
import io.facet.common.dsl.and
import io.facet.common.flowOf
import io.facet.common.reply
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import xyz.swagbot.extensions.*
import xyz.swagbot.features.music.getFormattedTime
import xyz.swagbot.util.baseTemplate

object Queue : ChatCommand(
    name = "View Queue",
    aliases = setOf("queue"),
    scope = Scope.GUILD,
    category = "music",
    description = "",
    usage = commandUsage {
        default("")
    }
) {

    private val endLeft = ReactionEmoji.unicode("⏮️")
    private val nextLeft = ReactionEmoji.unicode("◀️")
    private val nextRight = ReactionEmoji.unicode("▶️")
    private val endRight = ReactionEmoji.unicode("⏭️")

    private val pageLeft = listOf(endLeft, nextLeft)
    private val pageRight = listOf(nextRight, endRight)

    private val allReactions = pageLeft + pageRight

    override fun DSLCommandNode<ChatCommandSource>.register() {
        runs {
            val scheduler = getGuild().trackScheduler
            val currentTrack: AudioTrack? = scheduler.player.playingTrack
            val queue = scheduler.queue
                .map { it to client.getMemberById(guildId!!, it.context.requesterId).awaitNullable() }

            if (currentTrack == null || queue.isEmpty()) {
                message.reply(
                    "The queue is empty! Go add a video or song with the `${prefixUsed}play` or " +
                        "`${prefixUsed}search` commands!"
                )
                return@runs
            }

            val queueMessage = message.reply(
                queueEmbed(
                    currentTrack,
                    queue,
                    0,
                    scheduler.shouldAutoplay,
                    scheduler.shouldLoop,
                    scheduler.player.isPaused,
                    scheduler.queueTimeLeft
                )
            )
            allReactions.forEach { queueMessage.addReaction(it).await() }

            val pagesActor = actor<ReactionAddEvent> {
                var currentPage = 0
                for (event in channel) {
                    val queue = scheduler.queue
                        .map { it to client.getMemberById(guildId!!, it.context.requesterId).awaitNullable() }
                    val maxPage = queue.size / 10
                    when (event.emoji) {
                        endLeft -> currentPage = 0
                        nextLeft -> currentPage--
                        nextRight -> currentPage++
                        endRight -> currentPage = maxPage
                    }
                    if (currentPage in 0..maxPage) {
                        queueMessage.edit()
                            .withEmbeds(
                                queueEmbed(
                                    scheduler.player.playingTrack,
                                    queue,
                                    currentPage,
                                    scheduler.shouldAutoplay,
                                    scheduler.shouldLoop,
                                    scheduler.player.isPaused,
                                    scheduler.queueTimeLeft
                                )
                            )
                            .await()
                    } else {
                        when {
                            currentPage > maxPage -> currentPage = maxPage
                            currentPage < 0 -> currentPage = 0
                        }
                    }
                    queueMessage.removeReaction(event.emoji, event.userId).await()
                }
                queueMessage.removeAllReactions().await()
            }

            val reactionEvents = client.flowOf<ReactionAddEvent>()
                .filter { it.messageId == queueMessage.id }

            val pagesJob = reactionEvents
                .filter { it.userId == member.id && it.emoji in allReactions }
                .onEach { pagesActor.send(it) }
                .launchIn(this)

            val cleanupJob = reactionEvents
                .filter { it.userId != client.selfId && (it.userId != member.id || it.emoji !in allReactions) }
                .onEach { queueMessage.removeReaction(it.emoji, it.userId).await() }
                .launchIn(this)

            launch {
                delay(60_000 * 60)
                pagesJob.cancel()
                cleanupJob.cancel()
                pagesActor.close()
            }
        }

        argument("any", StringArgumentType.greedyString()) {
            runs { context ->
                message.reply(
                    "`${prefixUsed}${context.aliasUsed}` is used to view queued tracks. Use `${prefixUsed}play` or " +
                        "`${prefixUsed}search` to add music to the queue."
                )
            }
        }
    }

    private fun queueEmbed(
        currentTrack: AudioTrack,
        tracks: List<Pair<AudioTrack, Member?>>,
        page: Int,
        autoplay: Boolean,
        loop: Boolean,
        paused: Boolean,
        queueLength: Long,
    ) = baseTemplate.and {
        title = ":musical_note: | Track Queue"

        val startIndex = page * 10
        description = tracks
            .subList(startIndex, (startIndex + 10).coerceAtMost(tracks.size))
            .mapIndexed { i, (track, member) ->
                "${startIndex + i + 1}. ${track.info.boldFormattedTitleWithLink} - " +
                    "**${track.formattedLength}** (**${member?.displayName ?: "Unknown"}**)"
            }
            .joinToString("\n") + "\n"

        field {
            name = ":musical_note: Currently Playing"
            value = "${currentTrack.info.boldFormattedTitleWithLink} - " +
                "**${currentTrack.formattedPosition}** / **${currentTrack.formattedLength}**"
        }
        field("Autoplay", if (autoplay) ":white_check_mark:" else ":x:", true)
        field("Loop", if (loop) ":white_check_mark:" else ":x:", true)
        field("Paused", if (paused) ":white_check_mark:" else ":x:", true)
        field("In Queue", "${tracks.size}", true)
        field("Queue Length", getFormattedTime(queueLength / 1000), true)
        field("Page", "${page + 1} / ${tracks.size / 10 + 1}", true)
    }
}
