package xyz.swagbot.commands.music

import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Command
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.EmbedBuilder
import xyz.swagbot.commands.getBotLockedMessage
import xyz.swagbot.database.isBotLocked
import xyz.swagbot.database.trackHandler
import xyz.swagbot.utils.BLUE
import java.awt.Color

object ShuffleCommand : Command("Shuffle", "shuffle", scope = Command.Scope.GUILD) {

    init {
        help.usage[""] = "Shuffle the tracks in the queue."
    }

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent,
                         builder: AdvancedMessageBuilder): AdvancedMessageBuilder {
        if (event.guild.isBotLocked)
            return getBotLockedMessage(builder)
        val embed = EmbedBuilder()
        val audioHandler = event.guild.trackHandler
        if (audioHandler.getQueue().isEmpty())
            embed.withColor(Color.RED).withDesc("There are no tracks in the queue to shuffle!")
        else {
            audioHandler.shuffleQueue()
            embed.withColor(BLUE).withDesc("Shuffled the queue.")
        }
        return builder.withEmbed(embed)
    }
}