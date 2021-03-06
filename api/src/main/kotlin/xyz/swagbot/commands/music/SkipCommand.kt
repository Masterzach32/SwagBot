package xyz.swagbot.commands.music

import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Command
import net.masterzach32.commands4k.Permission
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.EmbedBuilder
import xyz.swagbot.Stats
import xyz.swagbot.commands.getBotLockedMessage
import xyz.swagbot.database.commandPrefix
import xyz.swagbot.database.getBotPermission
import xyz.swagbot.database.isBotLocked
import xyz.swagbot.database.trackHandler
import xyz.swagbot.dsl.getBoldFormattedTitle
import xyz.swagbot.utils.BLUE
import xyz.swagbot.utils.RED

object SkipCommand : Command("Skip", "skip", "s", scope = Scope.GUILD) {

    init {
        help.desc = "Skip the current song in the queue."
    }

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent,
                         builder: AdvancedMessageBuilder): AdvancedMessageBuilder {
        if (event.guild.isBotLocked)
            return getBotLockedMessage(builder)
        val embed = EmbedBuilder()

        if (event.author.getBotPermission(event.guild) < Permission.MOD)
            return builder.withEmbed(embed.withColor(RED).withDesc("You don't have permission to instantly " +
                    "skip a track. Try using `${event.guild.commandPrefix}voteskip` instead."))

        val skippedTrack = event.guild.trackHandler.playNext()
        if (skippedTrack == null)
            return builder.withEmbed(embed.withColor(RED).withDesc("Cannot skip as there is no track playing!"))
        Stats.TRACKS_SKIPPED.addStat()
        return builder.withEmbed(embed.withColor(BLUE).withDesc("Skipped track: ${skippedTrack.getBoldFormattedTitle()}"))
    }
}