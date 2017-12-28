package xyz.swagbot.commands.music

import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Command
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import xyz.swagbot.database.getAudioHandler

object SkipCommand : Command("Skip", "skip", "s", scope = Scope.GUILD) {

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent,
                         builder: AdvancedMessageBuilder): AdvancedMessageBuilder? {

        event.guild.getAudioHandler()!!.playNext()

        return null
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("", "Skips the currently playing song.")
    }
}