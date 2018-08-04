package xyz.swagbot.commands.normal

import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Command
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import xyz.swagbot.Stats
import xyz.swagbot.commands.getWrongArgumentsMessage
import xyz.swagbot.utils.getContent
import java.net.URLEncoder

object LmgtfyCommand : Command("Let Me Google that for You", "lmgtfy", "google") {

    init {
        help.usage["<search query>"] = "Google anything."
    }

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent,
                         builder: AdvancedMessageBuilder): AdvancedMessageBuilder {
        if(args.isEmpty())
            return getWrongArgumentsMessage(builder, this, cmdUsed)
        Stats.LMGTFY_SEARCH.addStat()
        return builder.withContent("http://www.lmgtfy.com/?q=${URLEncoder.encode(getContent(args, 0).toLowerCase(), "UTF-8")}")
    }
}