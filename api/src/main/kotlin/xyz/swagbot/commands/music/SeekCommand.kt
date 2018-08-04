package xyz.swagbot.commands.music

import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Command
import net.masterzach32.commands4k.RED
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.EmbedBuilder
import xyz.swagbot.commands.getBotLockedMessage
import xyz.swagbot.commands.getWrongArgumentsMessage
import xyz.swagbot.database.isBotLocked
import xyz.swagbot.database.trackHandler
import xyz.swagbot.dsl.*
import xyz.swagbot.utils.BLUE

object SeekCommand : Command("Seek Track", "seek", scope = Scope.GUILD) {

    init {
        help.usage["<position>"] = "Seek to the designated position in the track. Time formats: (hh:mm:ss), (mm:ss)," +
                " (ss)"
    }

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent,
                         builder: AdvancedMessageBuilder): AdvancedMessageBuilder {
        if (event.guild.isBotLocked)
            return getBotLockedMessage(builder)
        if (args.isEmpty())
            return getWrongArgumentsMessage(builder, this, cmdUsed)
        val embed = EmbedBuilder()
        val playingTrack = event.guild.trackHandler.player.playingTrack

        if (playingTrack == null)
            return builder.withEmbed(embed.withColor(RED).withDesc("Cannot seek track as there is no track currently playing!"))
        if (!playingTrack.isSeekable)
            return builder.withEmbed(embed.withColor(RED).withDesc("Current track is not seekable."))

        val split = args[0].split(":").toMutableList()
        var seconds = 0
        var multiplier = 1

        while (split.isNotEmpty()) {
            seconds += multiplier * split.removeAt(split.size-1).toInt()
            multiplier *= 60
        }

        val ms = seconds * 1000

        if (ms < 0 || ms >= playingTrack.duration)
            return builder.withEmbed(embed.withColor(RED).withDesc("Specified position is out of range. " +
                    "(Track length is **${playingTrack.getFormattedLength()}**)"))

        val oldPos = playingTrack.getFormattedPosition()
        playingTrack.position = ms.toLong()

        embed.withColor(BLUE)
                .withTitle(":musical_note: | Seek track")
                .withDesc(playingTrack.getFormattedTitleAsLink())
                .appendDesc("\nAuthor/Channel: **${playingTrack.info.author}**")
                .appendDesc("\nSeek: (**$oldPos** / **${playingTrack.getFormattedLength()}**) -> " +
                        "(**${playingTrack.getFormattedPosition()}** / **${playingTrack.getFormattedLength()}**)")

        if (playingTrack.info.hasThumbnail())
            embed.withThumbnail(playingTrack.info.getThumbnailUrl())

        return builder.withEmbed(embed)
    }
}