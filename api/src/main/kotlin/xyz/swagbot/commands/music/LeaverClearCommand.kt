package xyz.swagbot.commands.music

import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Command
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import xyz.swagbot.commands.getBotLockedMessage
import xyz.swagbot.database.isBotLocked
import xyz.swagbot.database.trackHandler
import xyz.swagbot.utils.embedBlue
import xyz.swagbot.utils.embedRed

object LeaverClearCommand : Command("Leaver Cleanup", "leavercleanup", scope = Scope.GUILD) {

    init {
        help.usage[""] = "Prune tracks from the queue added by users that are no longer listening."
    }

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent,
                         builder: AdvancedMessageBuilder): AdvancedMessageBuilder {
        if (event.guild.isBotLocked)
            return getBotLockedMessage(builder)
        event.channel.toggleTypingStatus()

        if (event.guild.connectedVoiceChannel == null)
            return builder.withEmbed(embedRed("Leaver cleanup requires the bot to be in a voice channel!"))

        val removed = event.guild.trackHandler.pruneTracks(event.guild.connectedVoiceChannel.connectedUsers)

        return builder.withEmbed(embedBlue("Removed **${removed.size}** tracks from the queue."))
    }

}