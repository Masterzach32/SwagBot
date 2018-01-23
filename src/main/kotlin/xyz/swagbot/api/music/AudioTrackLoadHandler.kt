package xyz.swagbot.api.music

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.masterzach32.commands4k.AdvancedMessageBuilder
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.EmbedBuilder
import sx.blah.discord.util.RequestBuffer
import xyz.swagbot.dsl.getBoldFormattedTitle
import xyz.swagbot.utils.BLUE
import xyz.swagbot.utils.RED
import xyz.swagbot.utils.getFormattedTime

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
        handler.queue(track)
        embed.withColor(BLUE)
        embed.withDesc("${event.author} queued track: ${track.getBoldFormattedTitle()}. ")
        if (handler.getQueue().isNotEmpty())
            embed.appendDesc("Estimated time in queue: **${getFormattedTime((handler.getQueueLength()/1000).toInt())}**")
        else if (handler.getQueue().isEmpty() && handler.player.playingTrack != null)
            embed.appendDesc("Playing next!")
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
        }
        embed.withColor(BLUE)
        embed.withDesc("${event.author} queued playlist: ${playlist.name}")
        RequestBuffer.request { event.message.delete() }
        RequestBuffer.request { builder.withEmbed(embed).build() }
    }
}