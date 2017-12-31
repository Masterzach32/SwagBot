package xyz.swagbot.commands.music

import com.mashape.unirest.http.Unirest
import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Command
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.EmbedBuilder
import xyz.swagbot.api.getIdFromSearch
import xyz.swagbot.commands.Type
import xyz.swagbot.commands.getApiErrorMessage
import xyz.swagbot.commands.getWrongArgumentsMessage
import xyz.swagbot.database.getKey
import xyz.swagbot.utils.BLUE
import xyz.swagbot.utils.RED
import xyz.swagbot.utils.getContent
import org.json.JSONObject



object SearchCommand : Command("Search YouTube", "search") {

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent,
                         builder: AdvancedMessageBuilder): AdvancedMessageBuilder? {
        if (args.isEmpty())
            return getWrongArgumentsMessage(builder, this, cmdUsed)
        event.channel.toggleTypingStatus()
        val embed = EmbedBuilder()

        val identifier = getIdFromSearch(getContent(args, 0))

        if (identifier == null)
            return builder.withEmbed(embed.withColor(RED).withDesc("Sorry, I could not find a video that matched " +
                    "that description. Try refining your search."))

        val title = try {
            val apiCall = "https://www.googleapis.com/youtube/v3/videos?part=snippet&id=$identifier"
            val response = Unirest.get("$apiCall&key=${getKey("google_auth_key")}").asJson()
            if (response.status != 200)
                return getApiErrorMessage(builder, Type.GET, apiCall, response.body.toString(),
                        response.status, response.statusText)
            response.body.`object`.getJSONArray("items").getJSONObject(0)
                    .getJSONObject("snippet").getString("title")
        } catch (t: Throwable) {
            return builder.withEmbed(embed.withColor(RED).withDesc("Error while contacting the YouTube API: ${t.message}"))
        }

        return builder.withEmbed(embed.withColor(BLUE)
                .withDesc("Found video: **$title**\nhttps://youtube.com/watch?v=$identifier"))
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("<search query>", "Finds the video that best matches the description.")
    }
}