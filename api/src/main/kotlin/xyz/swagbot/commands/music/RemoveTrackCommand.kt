package xyz.swagbot.commands.music

import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Command
import net.masterzach32.commands4k.Permission
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.EmbedBuilder
import xyz.swagbot.commands.getBotLockedMessage
import xyz.swagbot.commands.getWrongArgumentsMessage
import xyz.swagbot.database.isBotLocked
import xyz.swagbot.database.trackHandler
import xyz.swagbot.dsl.getBoldFormattedTitle
import xyz.swagbot.utils.BLUE
import xyz.swagbot.utils.RED

object RemoveTrackCommand : Command("Remove Track", "removetrack", "remove", "rmtrack",
        scope = Scope.GUILD, botPerm = Permission.MOD) {

    init {
        help.usage["<index>"] = "Remove the specified track from the queue."
    }

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent,
                         builder: AdvancedMessageBuilder): AdvancedMessageBuilder {
        if (event.guild.isBotLocked)
            return getBotLockedMessage(builder)
        if (args.isEmpty())
            return getWrongArgumentsMessage(builder, this, cmdUsed)
        val index = try {
            args[0].toInt()
        } catch (e: NumberFormatException) {
            return getWrongArgumentsMessage(builder, this, cmdUsed)
        }
        val embed = EmbedBuilder()

        val removed = event.guild.trackHandler.removeTrack(index-1)
        if (removed != null)
            embed.withColor(BLUE).withDesc("Removed ${removed.getBoldFormattedTitle()} from the queue.")
        else
            embed.withColor(RED).withDesc("Try re-checking your track index.")

        return builder.withEmbed(embed)
    }
}