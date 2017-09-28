package xyz.swagbot.commands.normal

import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Command
import net.masterzach32.commands4k.Permission
import net.masterzach32.commands4k.getWrongArgumentsMessage
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import xyz.swagbot.utils.getContent
import java.net.URLEncoder

object LmgtfyCommand : Command("Let Me Google that for You", "lmgtfy", "google") {

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent, permission: Permission): AdvancedMessageBuilder {
        if(args.isEmpty())
            return getWrongArgumentsMessage(event.channel, this, cmdUsed)
        return AdvancedMessageBuilder(event.channel).withContent("http://www.lmgtfy.com/?q=${URLEncoder.encode(getContent(args, 0).toLowerCase(), "UTF-8")}")
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("<search query>", "Google anything.")
    }
}