package xyz.swagbot.commands.music

import com.mashape.unirest.http.Unirest
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Command
import org.json.JSONObject
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.EmbedBuilder
import sx.blah.discord.util.RequestBuffer
import xyz.swagbot.api.getVideoFromSearch
import xyz.swagbot.api.music.TrackScheduler
import xyz.swagbot.api.music.TrackUserData
import xyz.swagbot.audioPlayerManager
import xyz.swagbot.commands.getBotLockedMessage
import xyz.swagbot.commands.getWrongArgumentsMessage
import xyz.swagbot.database.getAudioHandler
import xyz.swagbot.database.getKey
import xyz.swagbot.database.isBotLocked
import xyz.swagbot.logger
import xyz.swagbot.utils.BLUE
import xyz.swagbot.utils.RED
import xyz.swagbot.utils.getContent
import java.net.URLEncoder

object PlayCommand : Command("Play", "play", "p", scope = Command.Scope.GUILD) {

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent,
                         builder: AdvancedMessageBuilder): AdvancedMessageBuilder? {
        if(args.isEmpty())
            return getWrongArgumentsMessage(builder, this, cmdUsed)
        if (event.guild.isBotLocked())
            return getBotLockedMessage(builder)
        event.channel.toggleTypingStatus()

        val handler = event.guild.getAudioHandler()

        val identifier = if (args[0].contains("http://") || args[0].contains("https://")) args[0]
        else {
            var content = getContent(args, 0)
            if (!content.contains("audio"))
                content += " audio"
            getVideoFromSearch(content)
        }

        if (identifier == null)
            return builder.withEmbed(EmbedBuilder().withColor(RED).withDesc("Sorry, I could not find a video that" +
                    " matched that description. Try refining your search."))

        audioPlayerManager.loadItem(identifier, AudioTrackLoadHandler(handler, event, builder))

        return null
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("<search query>", "Searches YouTube for the specified song.")
        usage.put("<url>", "Queues the specified song in the server's audio player.")
    }

    class AudioTrackLoadHandler(val player: TrackScheduler, val event: MessageReceivedEvent,
                                val builder: AdvancedMessageBuilder) : AudioLoadResultHandler {

        val embed = EmbedBuilder()

        override fun loadFailed(exception: FriendlyException) {
            logger.warn("Could not load track: ${exception.message}")
            embed.withColor(RED)
            embed.withDesc("Could not load track: ${exception.message}")
            RequestBuffer.request { builder.withEmbed(embed).build() }
        }

        override fun trackLoaded(track: AudioTrack) {
            track.userData = TrackUserData(event.author)
            player.queue(track)
            embed.withColor(BLUE)
            embed.withDesc("${event.author.mention()} queued track: **${track.info.title}** by **${track.info.author}**")
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
            embed.withDesc("${event.author.mention()} queued playlist: ${playlist.name}")
            RequestBuffer.request { event.message.delete() }
            RequestBuffer.request { builder.withEmbed(embed).build() }
        }
    }
}