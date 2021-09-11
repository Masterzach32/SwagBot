package xyz.swagbot.commands

import com.sedmelluq.discord.lavaplayer.track.AudioItem
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import io.facet.commands.GlobalGuildApplicationCommand
import io.facet.commands.GuildSlashCommandContext
import io.facet.commands.acknowledge
import io.facet.commands.applicationCommandRequest
import io.facet.common.*
import io.facet.common.dsl.and
import io.facet.core.feature
import xyz.swagbot.extensions.isPremium
import xyz.swagbot.extensions.joinWithAutoDisconnect
import xyz.swagbot.extensions.setTrackContext
import xyz.swagbot.extensions.trackScheduler
import xyz.swagbot.features.music.Music
import xyz.swagbot.features.music.trackRequestedTemplate
import xyz.swagbot.util.baseTemplate
import java.util.*

object Play : GlobalGuildApplicationCommand {

    override val request = applicationCommandRequest("play", "Queue up music and/or videos to be played by the bot.") {
        string(
            "query",
            "The name of the song/video, or url to the YouTube/Soundcloud/Audio file.",
            true
        )
    }

    override suspend fun GuildSlashCommandContext.execute() {
        val guild = getGuild()

        if (!guild.isPremium())
            return event.reply("Music is a premium feature of SwagBot").withEphemeral(true).await()

        if (member.voiceState.awaitNullable()?.channelId?.unwrap() == null)
            return event.reply("You must be in a voice channel to add music to the queue!").withEphemeral(true).await()

        acknowledge()

        val musicFeature = client.feature(Music)
        val query: String by options
        val urlMarkers = listOf("http://", "https://")
        val item: AudioItem? = try {
            if (urlMarkers.any { it in query })
                musicFeature.searchItem(query)
            else
                musicFeature.searchItem("ytsearch:$query")
        } catch (e: Throwable) {
            interactionResponse.createFollowupMessage("I couldn't find anything for *\"$query\"*.").await()
            return
        }

        val scheduler = guild.trackScheduler
        when (item) {
            is AudioTrack -> {
                item.setTrackContext(member, getChannel())
                scheduler.queue(item)

                interactionResponse.sendFollowupMessage(
                    trackRequestedTemplate(
                        member.displayName,
                        item,
                        scheduler.queueTimeLeft
                    )
                )

                if (getGuild().getConnectedVoiceChannel() == null)
                    member.getConnectedVoiceChannel()?.joinWithAutoDisconnect()
            }
            is AudioPlaylist -> {
                if (item.isSearchResult) {
                    val track = item.tracks.maxByOrNull { track ->
                        track.info.title.lowercase(Locale.getDefault()).let { "audio" in it || "lyrics" in it }
                    }

                    if (track != null) {
                        track.setTrackContext(member, getChannel())
                        scheduler.queue(track)

                        event.interactionResponse.sendFollowupMessage(
                            trackRequestedTemplate(
                                member.displayName,
                                track,
                                scheduler.queueTimeLeft
                            )
                        )
                        if (getGuild().getConnectedVoiceChannel() == null)
                            member.getConnectedVoiceChannel()?.joinWithAutoDisconnect()
                    } else
                        event.interactionResponse.createFollowupMessage("I couldn't find anything for *\"$query\"*.")
                } else {
                    item.tracks.forEach { track ->
                        track.setTrackContext(member, getChannel())
                        scheduler.queue(track)
                    }

                    event.interactionResponse.sendFollowupMessage(
                        baseTemplate.and {
                            title = ":musical_note: | Playlist requested by ${member.displayName}"
                            description = """
                                **${item.name}**
                                ${item.tracks.size} Tracks
                            """.trimIndent().trim()
                        }
                    )
                    if (getGuild().getConnectedVoiceChannel() == null)
                        member.getConnectedVoiceChannel()?.joinWithAutoDisconnect()
                }
            }
            else -> event.interactionResponse.createFollowupMessage("I couldn't find anything for *\"$query\"*.")
        }
    }
}
