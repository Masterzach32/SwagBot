package xyz.swagbot.commands.music

import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Command
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.EmbedBuilder
import xyz.swagbot.commands.getBotLockedMessage
import xyz.swagbot.database.getAudioHandler
import xyz.swagbot.database.isBotLocked
import xyz.swagbot.utils.BLUE

object RemoveDuplicatesCommand : Command("Remove Duplicate Tracks", "removedupes", scope = Scope.GUILD) {

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent,
                         builder: AdvancedMessageBuilder): AdvancedMessageBuilder? {
        if (event.guild.isBotLocked())
            return getBotLockedMessage(builder)

        return builder.withEmbed(EmbedBuilder().withColor(BLUE).withDesc("Removed " +
                "**${event.guild.getAudioHandler().removeDuplicates()}** duplicate tracks from the queue."))
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("", "Remove duplicate tracks from the queue.")
    }
}