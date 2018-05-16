package xyz.swagbot.api.music

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.masterzach32.commands4k.AdvancedMessageBuilder
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.EmbedBuilder
import sx.blah.discord.util.RequestBuffer
import xyz.swagbot.database.addTrackToDatabase
import xyz.swagbot.database.getTimezone
import xyz.swagbot.dsl.getFormattedLength
import xyz.swagbot.dsl.getThumbnailUrl
import xyz.swagbot.dsl.hasThumbnail
import xyz.swagbot.utils.BLUE
import xyz.swagbot.utils.RED
import xyz.swagbot.utils.getFormattedTime
import java.text.SimpleDateFormat
import java.util.*

class AudioTrackLoadHandler(val handler: TrackHandler, val event: MessageReceivedEvent,
                            val builder: AdvancedMessageBuilder) : AudioLoadResultHandler {

    val embed = EmbedBuilder()

    override fun loadFailed(exception: FriendlyException) {
        if (exception.severity != FriendlyException.Severity.COMMON)
            exception.printStackTrace()
        embed.withColor(RED)
        embed.withDesc("Could not load track: ${exception.message}")
        RequestBuffer.request { builder.withEmbed(embed).build() }
    }

    override fun trackLoaded(track: AudioTrack) {
        track.userData = TrackUserData(event.author)
        embed.withColor(BLUE)
        embed.withTitle(":musical_note: | Track requested by ${event.author.getDisplayName(event.guild)}")
        embed.withDesc("**[${track.info.title}](${track.info.uri})**\n")
        embed.appendDesc("Author/Channel: **${track.info.author}**\n")
        embed.appendDesc("Length: **${track.getFormattedLength()}**")

        if (track.info.hasThumbnail())
            embed.withThumbnail(track.info.getThumbnailUrl())

        if (handler.getQueue().isNotEmpty())
            embed.appendDesc("\nEstimated time until track is played: " +
                    "**${getFormattedTime((handler.getQueueLength()/1000).toInt())}**")

        val date = SimpleDateFormat("MM/dd/yy")
        val time = SimpleDateFormat("hh:mm:ss a")
        val now = Date()
        embed.withFooterText("${date.format(now)} at ${time.format(now)} ${event.guild.getTimezone()}")

        handler.queue(track)
        if (!track.info.isStream)
            event.author.addTrackToDatabase(track)
        RequestBuffer.request { event.message.delete() }
        RequestBuffer.request { builder.withEmbed(embed).build() }
    }

    override fun noMatches() {
        embed.withColor(RED)
        embed.withDesc("Sorry, I could not load your track. Try checking the url.")
        RequestBuffer.request { builder.withEmbed(embed).build() }
    }

    override fun playlistLoaded(playlist: AudioPlaylist) {
        for (track in playlist.tracks) {
            track.userData = TrackUserData(event.author)
            handler.queue(track)
            if (!track.info.isStream)
                event.author.addTrackToDatabase(track)
        }
        embed.withColor(BLUE)
        embed.withDesc("${event.author} queued playlist: **${playlist.name}** with **${playlist.tracks.size}** tracks.")
        RequestBuffer.request { event.message.delete() }
        RequestBuffer.request { builder.withEmbed(embed).build() }
    }
}