package xyz.swagbot.commands.music

import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Command
import net.masterzach32.commands4k.Permission
import net.masterzach32.commands4k.RED
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.EmbedBuilder
import xyz.swagbot.Stats
import xyz.swagbot.commands.getBotLockedMessage
import xyz.swagbot.database.isBotLocked
import xyz.swagbot.database.trackHandler
import xyz.swagbot.utils.BLUE

object SkipToCommand : Command("Skip To Track", "skipto", scope = Scope.GUILD,
        botPerm = Permission.MOD) {

    init {
        help.usage["<int>"] = "Skip to the specified track in the queue. If the integer specified is larger than the " +
                "number of queued tracks, then skip to the last track in the queue."
    }

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent,
                         builder: AdvancedMessageBuilder): AdvancedMessageBuilder {
        if (event.guild.isBotLocked)
            return getBotLockedMessage(builder)
        event.channel.toggleTypingStatus()
        val embed = EmbedBuilder()

        if (args.isEmpty())
            return builder.withEmbed(embed.withColor(RED).withDesc("You must specify a track to skip to!"))
        val skipped = event.guild.trackHandler.skipTo(args[0].toInt())
        Stats.TRACKS_SKIPPED.addStat(skipped.size)
        return builder.withEmbed(embed.withColor(BLUE).withDesc("Skipped **${skipped.size}** tracks."))
    }
}