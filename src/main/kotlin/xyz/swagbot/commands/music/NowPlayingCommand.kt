package xyz.swagbot.commands.music

import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Command
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.EmbedBuilder
import xyz.swagbot.database.getAudioHandler
import xyz.swagbot.database.getBotVolume
import xyz.swagbot.database.getCommandPrefix
import xyz.swagbot.database.logger
import xyz.swagbot.dsl.*
import xyz.swagbot.utils.BLUE
import xyz.swagbot.utils.RED

object NowPlayingCommand : Command("Now Playing", "nowplaying", "np", scope = Scope.GUILD) {

    init {
        help.usage[""] = "Display the currently playing track."
    }

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent,
                         builder: AdvancedMessageBuilder): AdvancedMessageBuilder {
        val playingTrack = event.guild.getAudioHandler().player.playingTrack
        val embed = EmbedBuilder()
        event.channel.toggleTypingStatus()
        if (playingTrack == null)
            return builder.withEmbed(embed.withColor(RED).withDesc("Im not playing anything right now. Go add some" +
                    " tracks with the ${event.guild.getCommandPrefix()}play or ${event.guild.getCommandPrefix()}search commands!"))
        embed.withColor(BLUE)
                .withTitle(":musical_note: ${playingTrack.getFormattedTitle()} " +
                        "(${playingTrack.getFormattedPosition()} / ${playingTrack.getFormattedLength()})")
                .withDesc("Requested by: ${playingTrack.getRequester().getDisplayName(event.guild)}")
                .appendDesc("\nVolume: **${event.guild.getBotVolume()}**")

        if (playingTrack.info.hasThumbnail())
            embed.withThumbnail(playingTrack.info.getThumbnailUrl())
        return builder.withEmbed(embed)
    }
}