package xyz.swagbot.commands

import com.sedmelluq.discord.lavaplayer.track.*
import discord4j.rest.util.*
import io.facet.core.extensions.*
import io.facet.discord.appcommands.*
import io.facet.discord.appcommands.extensions.*
import io.facet.discord.extensions.*
import xyz.swagbot.extensions.*
import xyz.swagbot.features.music.*
import xyz.swagbot.util.*
import java.util.*

object Play : GlobalGuildApplicationCommand {

    override val request = applicationCommandRequest("play", "Queue up music and/or videos to be played by the bot.") {
        addOption(
            "query",
            "The name of the song/video, or url to the YouTube/Soundcloud/Audio file.",
            ApplicationCommandOptionType.STRING,
            true
        )
    }

    override suspend fun GuildSlashCommandContext.execute() {
        val guild = getGuild()

        if (!guild.isPremium())
            return event.replyEphemeral("Music is a premium feature of SwagBot").await()

        if (member.voiceState.awaitNullable()?.channelId?.unwrap() == null)
            return event.replyEphemeral("You must be in a voice channel to add music to the queue!").await()

        acknowledge()

        val musicFeature = client.feature(Music)
        val query: String by options
        val item: AudioItem? = try {
            if ("http://" in query || "https://" in query)
                musicFeature.searchItem(query)
            else
                musicFeature.searchItem("ytsearch:$query")
        } catch (e: Throwable) {
            event.interactionResponse.createFollowupMessage("I couldn't find anything for *\"$query\"*.").await()
            return
        }

        val scheduler = guild.trackScheduler
        when (item) {
            is AudioTrack -> {
                item.setTrackContext(member, getChannel())
                scheduler.queue(item)

                createFollowupMessage(
                    trackRequestedTemplate(
                        member.displayName,
                        item,
                        scheduler.queueTimeLeft
                    )
                )

                if (getGuild().getConnectedVoiceChannel() == null)
                    member.getConnectedVoiceChannel()?.join()
            }
            is AudioPlaylist -> {
                if (item.isSearchResult) {
                    val track = item.tracks.maxByOrNull { track ->
                        track.info.title.lowercase(Locale.getDefault()).let { "audio" in it || "lyrics" in it }
                    }

                    if (track != null) {
                        track.setTrackContext(member, getChannel())
                        scheduler.queue(track)

                        createFollowupMessage(
                            trackRequestedTemplate(
                                member.displayName,
                                track,
                                scheduler.queueTimeLeft
                            )
                        )
                        if (getGuild().getConnectedVoiceChannel() == null)
                            member.getConnectedVoiceChannel()?.join()
                    } else
                        event.interactionResponse.createFollowupMessage("I couldn't find anything for *\"$query\"*.")
                } else {
                    item.tracks.forEach { track ->
                        track.setTrackContext(member, getChannel())
                        scheduler.queue(track)
                    }

                    createFollowupMessage(baseTemplate.andThen {
                        title = ":musical_note: | Playlist requested by ${member.displayName}"
                        description = """
                                **${item.name}**
                                ${item.tracks.size} Tracks
                            """.trimIndent().trim()
                    })
                    if (getGuild().getConnectedVoiceChannel() == null)
                        member.getConnectedVoiceChannel()?.join()
                }
            }
            else -> event.interactionResponse.createFollowupMessage("I couldn't find anything for *\"$query\"*.")
        }
    }
}