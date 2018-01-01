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
import xyz.swagbot.api.getIdFromSearch
import xyz.swagbot.api.getVideoFromSearch
import xyz.swagbot.api.music.AudioTrackLoadHandler
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
            getVideoFromSearch(content)?.getUrl()
        }

        if (identifier == null)
            return builder.withEmbed(EmbedBuilder().withColor(RED).withDesc("Sorry, I could not find a video that" +
                    " matched that description. Try refining your search."))

        audioPlayerManager.loadItem(identifier, AudioTrackLoadHandler(handler, event, builder))

        return null
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("<search query>", "Searches YouTube for the best matching track and queues it.")
        usage.put("<url>", "Queues the specified track or stream in the server's audio player.")
    }
}