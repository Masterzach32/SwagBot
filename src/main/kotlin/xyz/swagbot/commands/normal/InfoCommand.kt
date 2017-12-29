package xyz.swagbot.commands.normal

import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Command
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

object InfoCommand : Command("Info", "info") {

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent,
                         builder: AdvancedMessageBuilder): AdvancedMessageBuilder {

        return builder
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("", "Get information about SwagBot.")
    }
}