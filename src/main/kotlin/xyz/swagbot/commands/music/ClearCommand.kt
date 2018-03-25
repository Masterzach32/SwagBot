package xyz.swagbot.commands.music

import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Command
import net.masterzach32.commands4k.Permission
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import xyz.swagbot.commands.getBotLockedMessage
import xyz.swagbot.database.getAudioHandler
import xyz.swagbot.database.isBotLocked

object ClearCommand : Command("Clear Queue", "clear", scope = Scope.GUILD, botPerm = Permission.MOD) {

    init {
        help.usage[""] = "Clears the queue of all tracks."
    }

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent,
                         builder: AdvancedMessageBuilder): AdvancedMessageBuilder? {
        if (event.guild.isBotLocked())
            return getBotLockedMessage(builder)

        event.guild.getAudioHandler().clearQueue()
        event.guild.getAudioHandler().playNext()

        return null
    }
}