package xyz.swagbot.commands.normal

import com.mashape.unirest.http.Unirest
import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Command
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import xyz.swagbot.commands.Type
import xyz.swagbot.commands.getApiErrorMessage
import xyz.swagbot.utils.withImage

/*
 * SwagBot - Created on 11/17/2017
 * Author: Zach Kozar
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at the project root.
 */

/**
 * @author Zach Kozar
 * @version 11/17/2017
 */
object CatCommand : Command("Random Cat Picture", "cat") {

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent,
                         builder: AdvancedMessageBuilder): AdvancedMessageBuilder {
        event.channel.toggleTypingStatus()
        val url = "http://random.cat/meow"
        val response = Unirest.get(url).asJson()
        if (response.status != 200)
            return getApiErrorMessage(builder, Type.GET, url, "none", response.status, response.statusText)
        return builder.withImage(response.body.`object`.getString("file"))
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {

    }
}