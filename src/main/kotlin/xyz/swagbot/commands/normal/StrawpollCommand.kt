package xyz.swagbot.commands.normal

import com.mashape.unirest.http.Unirest
import net.masterzach32.commands4k.*
import org.json.JSONArray
import org.json.JSONObject
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.EmbedBuilder
import xyz.swagbot.commands.Type
import xyz.swagbot.commands.getApiErrorMessage
import xyz.swagbot.commands.getWrongArgumentsMessage
import xyz.swagbot.utils.BLUE
import xyz.swagbot.utils.delimitWithoutEmpty
import xyz.swagbot.utils.getContent
import java.util.*

object StrawpollCommand : Command("Strawpoll", "strawpoll", "spoll") {

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent,
                         builder: AdvancedMessageBuilder): AdvancedMessageBuilder {

        event.channel.toggleTypingStatus()
        val choices = delimitWithoutEmpty(getContent(args, 0), "\\|")
        if (choices.size < 3 || choices.size > 31)
            return getWrongArgumentsMessage(builder, this, cmdUsed)
        choices.forEach { it.trim().replace("\n", "") }

        val body = JSONObject()
        body.put("title", choices[0])
        body.put("options", JSONArray(Arrays.copyOfRange(choices, 1, choices.size)))
        body.put("dupcheck", "normal")
        body.put("multi", false)
        body.put("captcha", true)

        val pollUrl = "http://www.strawpoll.me/api/v2/polls"
        val response = Unirest.post(pollUrl)
                .body(body.toString())
                .asJson()

        if (response.status != 200)
            return getApiErrorMessage(builder, Type.POST, pollUrl, body.toString(2), response.status,
                    response.statusText)

        val json = response.body.`object`
        val id = json.getInt("id")
        val embed = EmbedBuilder().withColor(BLUE)
                .withTitle(choices[0])
                .withUrl("https://strawpoll.me/$id")
                .withDesc("")
                .withFooterText("Strawpoll by ${event.author.getDisplayName(event.guild)}")
                .withFooterIcon(event.author.avatarURL)
        choices.drop(1).forEach { embed.appendDesc(":ballot_box_with_check: $it\n") }
        return builder.withEmbed(embed)
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("<title> | <option 1> | <option 2> [| [option 3]]", "Create a strawpoll with the title and " +
                "options. You must have at least two options and no more than 30.")
    }
}