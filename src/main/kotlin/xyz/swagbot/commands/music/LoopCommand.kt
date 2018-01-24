package xyz.swagbot.commands.music

import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Command
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.EmbedBuilder
import xyz.swagbot.commands.getBotLockedMessage
import xyz.swagbot.database.isBotLocked
import xyz.swagbot.database.toggleQueueLoop
import xyz.swagbot.utils.BLUE

object LoopCommand : Command("Loop Queue", "loop", scope = Scope.GUILD) {

    init {
        help.usage[""] = "Requeue tracks when they end."
    }

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent,
                         builder: AdvancedMessageBuilder): AdvancedMessageBuilder {
        if (event.guild.isBotLocked())
            return getBotLockedMessage(builder)
        val embed = EmbedBuilder().withColor(BLUE)

        val shouldLoop = event.guild.toggleQueueLoop()

        if (shouldLoop)
            return builder.withEmbed(embed.withDesc("Queue loop **enabled**."))
        return builder.withEmbed(embed.withDesc("Queue loop **disabled**."))
    }
}