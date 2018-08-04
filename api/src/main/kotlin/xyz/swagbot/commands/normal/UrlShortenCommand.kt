package xyz.swagbot.commands.normal

import com.mashape.unirest.http.Unirest
import net.masterzach32.commands4k.*
import org.json.JSONObject
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.EmbedBuilder
import xyz.swagbot.commands.Type
import xyz.swagbot.commands.getApiErrorMessage
import xyz.swagbot.commands.getWrongArgumentsMessage
import xyz.swagbot.database.getKey
import xyz.swagbot.utils.BLUE

object UrlShortenCommand : Command("URL Shortener", "goo.gl", "tinyurl") {

    init {
        help.usage["<link>"] = "Create a goo.gl or tinyurl link based on which alias is used."
    }

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent,
                         builder: AdvancedMessageBuilder): AdvancedMessageBuilder? {
        if (args.isEmpty())
            return getWrongArgumentsMessage(builder, this, cmdUsed)

        event.channel.toggleTypingStatus()
        val embed = EmbedBuilder().withColor(BLUE)
        val url = args[0]

        when (cmdUsed) {
            "goo.gl" -> {
                val post = "https://www.googleapis.com/urlshortener/v1/url?key=" + getKey("google_auth_key")
                val obj = JSONObject()
                obj.put("longUrl", url)

                val response = Unirest.post(post).header("Content-Type", "application/json").body(obj.toString()).asJson()
                if (response.status != 200)
                    return getApiErrorMessage(builder, Type.POST, post, obj.toString(2), response.status, response.statusText)
                return builder.withEmbed( embed.withDesc("Shortened link: " + response.body.`object`.getString("id")))
            }
            "tinyurl" -> {
                val post = "http://tinyurl.com/api-create.php?url=$url"
                val response = Unirest.get(post).asString()
                if (response.status != 200)
                    return getApiErrorMessage(builder, Type.GET, post, response.body, response.status, response.statusText)
                return builder.withEmbed(embed.withDesc("Shortened link: " + response.body))
            }
            else -> {
                return null
            }
        }
    }
}