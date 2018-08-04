package xyz.swagbot.api.music

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.masterzach32.commands4k.AdvancedMessageBuilder
import sx.blah.discord.handle.obj.IGuild
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.handle.obj.IUser
import sx.blah.discord.util.EmbedBuilder
import sx.blah.discord.util.RequestBuffer
import xyz.swagbot.database.addTrackToDatabase
import xyz.swagbot.dsl.getFormattedLength
import xyz.swagbot.dsl.getThumbnailUrl
import xyz.swagbot.dsl.hasThumbnail
import xyz.swagbot.utils.BLUE
import xyz.swagbot.utils.RED
import xyz.swagbot.utils.getFormattedTime
import java.time.Instant

class AudioTrackLoadHandler(
        val handler: TrackHandler,
        val requester: IUser,
        val guild: IGuild,
        val message: IMessage?,
        val builder: AdvancedMessageBuilder
) : AudioLoadResultHandler {

    val embed = EmbedBuilder()

    override fun loadFailed(exception: FriendlyException) {
        if (exception.severity != FriendlyException.Severity.COMMON)
            exception.printStackTrace()
        embed.withColor(RED)
        embed.withDesc("Could not load track: ${exception.message}")
        RequestBuffer.request { builder.withEmbed(embed).build() }
    }

    override fun trackLoaded(track: AudioTrack) {
        track.userData = TrackUserData(requester)
        embed.withColor(BLUE)
        embed.withTitle(":musical_note: | Track requested by ${requester.getDisplayName(guild)}")
        embed.withDesc("**[${track.info.title}](${track.info.uri})**\n")
        embed.appendDesc("Author/Channel: **${track.info.author}**\n")
        embed.appendDesc("Length: **${if (track.info.isStream) "Stream (duration unknown)" else track.getFormattedLength()}**")

        if (track.info.hasThumbnail())
            embed.withThumbnail(track.info.getThumbnailUrl())

        if (handler.getQueue().isNotEmpty())
            embed.appendDesc("\nEstimated time until track is played: " +
                    "**${getFormattedTime((handler.getQueueLength()/1000).toInt())}**")

        embed.withTimestamp(Instant.now())

        handler.queue(track)
        if (!track.info.isStream)
            requester.addTrackToDatabase(track)
        RequestBuffer.request { builder.withEmbed(embed).build() }
    }

    override fun noMatches() {
        embed.withColor(RED)
        embed.withDesc("Sorry, I could not load your track. Try checking the url.")
        RequestBuffer.request { builder.withEmbed(embed).build() }
    }

    override fun playlistLoaded(playlist: AudioPlaylist) {
        for (track in playlist.tracks) {
            track.userData = TrackUserData(requester)
            handler.queue(track)
            if (!track.info.isStream)
                requester.addTrackToDatabase(track)
        }
        embed.withColor(BLUE)
        embed.withDesc("$requester queued playlist: **${playlist.name}** with **${playlist.tracks.size}** tracks.")
        RequestBuffer.request { builder.withEmbed(embed).build() }
    }
}