package xyz.swagbot.commands.normal

import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Command
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import xyz.swagbot.commands.getWrongArgumentsMessage
import xyz.swagbot.utils.getContent
import java.net.URLEncoder

object LmgtfyCommand : Command("Let Me Google that for You", "lmgtfy", "google") {

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent,
                         builder: AdvancedMessageBuilder): AdvancedMessageBuilder {
        if(args.isEmpty())
            return getWrongArgumentsMessage(builder, this, cmdUsed)
        return builder.withContent("http://www.lmgtfy.com/?q=${URLEncoder.encode(getContent(args, 0).toLowerCase(), "UTF-8")}")
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("<search query>", "Google anything.")
    }
}