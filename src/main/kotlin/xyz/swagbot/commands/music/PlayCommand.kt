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
import xyz.swagbot.api.music.TrackScheduler
import xyz.swagbot.audioPlayerManager
import xyz.swagbot.commands.getWrongArgumentsMessage
import xyz.swagbot.database.getAudioHandler
import xyz.swagbot.database.getKey
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
        event.channel.toggleTypingStatus()

        val handler = event.guild.getAudioHandler()!!

        val identifier = if (args[0].contains("http://") || args[0].contains("https://")) args[0] else getVideoFromSearch(getContent(args, 0))

        audioPlayerManager.loadItem(identifier, AudioTrackLoadHandler(handler, event, builder))

        return null
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("<search query>", "Searches YouTube for the specified song.")
        usage.put("<url>", "Queues the specified song in the server's audio player.")
    }

    private fun getVideoFromSearch(search: String): String? {
        val response = Unirest.get("https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=50&q=" +
                URLEncoder.encode(search, "UTF-8") + "&key=" + getKey("google_auth_key")).asJson()
        if (response.status != 200)
            return null
        val json: JSONObject
        json = response.body.array.getJSONObject(0)
        if (json.has("items") && json.getJSONArray("items").length() > 0 &&
                json.getJSONArray("items").getJSONObject(0).has("id") &&
                json.getJSONArray("items").getJSONObject(0).getJSONObject("id").has("videoId"))
            return "https://youtube.com/watch?v=" +
                    json.getJSONArray("items").getJSONObject(0).getJSONObject("id").getString("videoId")
        return null
    }

    class AudioTrackLoadHandler(val player: TrackScheduler, val event: MessageReceivedEvent,
                                val builder: AdvancedMessageBuilder) : AudioLoadResultHandler {

        val embed = EmbedBuilder()

        override fun loadFailed(exception: FriendlyException) {
            embed.withColor(RED)
            embed.withDesc("Could not load track: ${exception.message}")
            RequestBuffer.request { builder.withEmbed(embed).build() }
        }

        override fun trackLoaded(track: AudioTrack) {
            track.userData = event.author
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
            embed.withColor(RED)
            embed.withDesc("Playlists are not supported at the moment.")
            RequestBuffer.request { builder.withEmbed(embed).build() }
        }
    }
}