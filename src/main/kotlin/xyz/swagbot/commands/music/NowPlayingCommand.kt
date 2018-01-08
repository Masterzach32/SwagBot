package xyz.swagbot.commands.music

import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Command
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.EmbedBuilder
import xyz.swagbot.database.getAudioHandler
import xyz.swagbot.database.getCommandPrefix
import xyz.swagbot.dsl.getBoldFormattedTitle
import xyz.swagbot.dsl.getFormattedLength
import xyz.swagbot.dsl.getFormattedPosition
import xyz.swagbot.dsl.getTrackUserData
import xyz.swagbot.utils.BLUE
import xyz.swagbot.utils.RED

object NowPlayingCommand : Command("Now Playing", "nowplaying", "np", scope = Scope.GUILD) {

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent,
                         builder: AdvancedMessageBuilder): AdvancedMessageBuilder {
        val playingTrack = event.guild.getAudioHandler().player.playingTrack
        val embed = EmbedBuilder()
        if (playingTrack == null)
            return builder.withEmbed(embed.withColor(RED).withDesc("Im not playing anything right now. Go add some" +
                    " tracks with the ${event.guild.getCommandPrefix()}play or ${event.guild.getCommandPrefix()}search commands!"))
        return builder.withEmbed(embed.withColor(BLUE).withDesc("Currently playing " +
                "${playingTrack.getBoldFormattedTitle()} - **${playingTrack.getFormattedPosition()}**" +
                " / **${playingTrack.getFormattedLength()}** " +
                "(${playingTrack.getTrackUserData().author.getDisplayName(event.guild)})"))
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("", "Display the currently playing track.")
    }
}