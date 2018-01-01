package xyz.swagbot.commands.music

import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Command
import net.masterzach32.commands4k.Permission
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.EmbedBuilder
import xyz.swagbot.commands.getBotLockedMessage
import xyz.swagbot.commands.getWrongArgumentsMessage
import xyz.swagbot.database.getAudioHandler
import xyz.swagbot.database.isBotLocked
import xyz.swagbot.utils.BLUE
import xyz.swagbot.utils.RED

object MoveTrackCommand : Command("Move Track", "move", scope = Scope.GUILD, botPerm = Permission.MOD) {

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent,
                         builder: AdvancedMessageBuilder): AdvancedMessageBuilder {
        if (event.guild.isBotLocked())
            return getBotLockedMessage(builder)
        if (args.isEmpty() || args.size > 2)
            return getWrongArgumentsMessage(builder, this, cmdUsed)

        val index0 = try {
            args[0].toInt()
        } catch (t: Throwable) {
            return getWrongArgumentsMessage(builder, this, cmdUsed)
        }

        val embed = EmbedBuilder()

        if (index0 <= 0 || index0 > event.guild.getAudioHandler().getQueue().size)
            return builder.withEmbed(embed.withColor(RED).withDesc("Initial track index is out of range. " +
                    "(Your index: **$index0**, Queue size: **${event.guild.getAudioHandler().getQueue().size}**)"))

        if (args.size == 1) {
            val track = event.guild.getAudioHandler().moveTrack(index0-1, 0)
            return builder.withEmbed(embed.withColor(BLUE).withDesc("Moved **${track.info.title}** to position **1**."))
        }

        val index1 = try {
            args[1].toInt()
        } catch (t: Throwable) {
            return getWrongArgumentsMessage(builder, this, cmdUsed)
        }

        if (index1-1 < 0 || index1-1 >= event.guild.getAudioHandler().getQueue().size)
            builder.withEmbed(embed.withColor(RED).withDesc("Final track index is out of range. " +
                    "(Your index: **$index0**, Queue size: **${event.guild.getAudioHandler().getQueue().size})"))

        val track = event.guild.getAudioHandler().moveTrack(index0-1, index1-1)
        return builder.withEmbed(embed.withColor(BLUE).withDesc("Moved **${track.info.title}** by " +
                "**${track.info.author}** from position **$index0** to position **$index1**."))
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("<index>", "Move the selected index to the front of the queue.")
        usage.put("<index 1> <index 2>", "Move track at index 1 to index 2.")
    }
}