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
import xyz.swagbot.logger
import xyz.swagbot.utils.BLUE
import xyz.swagbot.utils.RED

class AudioTrackLoadHandler(val player: TrackHandler, val event: MessageReceivedEvent,
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
        player.queue(track)
        embed.withColor(BLUE)
        embed.withDesc("${event.author} queued track: ${track.getBoldFormattedTitle()}")
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
            player.queue(track)
        }
        embed.withColor(BLUE)
        embed.withDesc("${event.author} queued playlist: ${playlist.name}")
        RequestBuffer.request { event.message.delete() }
        RequestBuffer.request { builder.withEmbed(embed).build() }
    }
}