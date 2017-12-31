package xyz.swagbot.commands.music

import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Command
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import xyz.swagbot.commands.getBotLockedMessage
import xyz.swagbot.database.isBotLocked

object VoteSkipCommand : Command("Vote Skip", "voteskip", "vskip", scope = Scope.GUILD) {

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent,
                         builder: AdvancedMessageBuilder): AdvancedMessageBuilder? {
        if (event.guild.isBotLocked())
            return getBotLockedMessage(builder)

        return null
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("", "Vote to skip a song. Requires a simple majority. (greater than 50%)")
    }
}