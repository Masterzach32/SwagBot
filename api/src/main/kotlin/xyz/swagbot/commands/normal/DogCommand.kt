package xyz.swagbot.commands.normal

import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.JsonNode
import com.mashape.unirest.http.Unirest
import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Command
import org.apache.http.conn.ConnectTimeoutException
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import xyz.swagbot.Stats
import xyz.swagbot.commands.Type
import xyz.swagbot.commands.getApiErrorMessage
import xyz.swagbot.utils.getContent
import xyz.swagbot.utils.withImage
import java.net.URLEncoder

object DogCommand : Command("Dog Pictures", "dog", "randomdog") {

    init {
        help.desc = "Finds a picture of a dog from the internet."
        help.usage[""] = "Finds a picture of a dog from a random breed."
        help.usage["<breed>"] = "Finds a picture of a dog of the breed specified."
    }

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent,
                         builder: AdvancedMessageBuilder): AdvancedMessageBuilder {
        event.channel.toggleTypingStatus()
        val url = "https://dog.ceo/api"
        val response: HttpResponse<JsonNode>
        response = try {
            if (args.isEmpty())
                Unirest.get("$url/breeds/image/random").asJson()
            else
                Unirest.get("$url/breed/${URLEncoder.encode(getContent(args, 0), "UTF-8")}/images/random").asJson()
        } catch (e: ConnectTimeoutException) {
            return builder.withContent("Sorry, but i'm having trouble connecting to $url at the moment.")
        }
        if (response.status != 200)
            return getApiErrorMessage(builder, Type.GET, url, "none", response.status, response.statusText)
        Stats.DOGS_FETCHED.addStat()
        return builder.withImage(response.body.`object`.getString("message"))
    }
}