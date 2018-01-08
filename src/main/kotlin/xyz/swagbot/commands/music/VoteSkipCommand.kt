package xyz.swagbot.commands.music

import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Command
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.EmbedBuilder
import xyz.swagbot.commands.getBotLockedMessage
import xyz.swagbot.database.getAudioHandler
import xyz.swagbot.database.isBotLocked
import xyz.swagbot.dsl.getBoldFormattedTitle
import xyz.swagbot.dsl.getTrackUserData
import xyz.swagbot.utils.BLUE
import xyz.swagbot.utils.RED

object VoteSkipCommand : Command("Vote Skip", "voteskip", "vskip", scope = Scope.GUILD) {

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent,
                         builder: AdvancedMessageBuilder): AdvancedMessageBuilder {
        if (event.guild.isBotLocked())
            return getBotLockedMessage(builder)
        val embed = EmbedBuilder()
        val playingTrack = event.guild.getAudioHandler().player.playingTrack
        if (playingTrack == null)
            return builder.withEmbed(embed.withColor(RED).withDesc("Cannot skip as there is no track playing!"))
        if (!playingTrack.getTrackUserData().addSkipVote(event.author))
            return builder.withEmbed(embed.withColor(RED).withDesc("You have already voted to skip this track!"))
        val skipThreshold = Math.round((event.guild.connectedVoiceChannel.connectedUsers.size-1)/2.0) -
                playingTrack.getTrackUserData().getSkipVoteCount()
        if (skipThreshold <= 0) {
            event.guild.getAudioHandler().playNext()
            return builder.withEmbed(embed.withColor(BLUE).withDesc("Skipped track ${playingTrack.getBoldFormattedTitle()}"))
        }
        return builder.withEmbed(embed.withColor(BLUE).withDesc("**$skipThreshold** more votes needed to skip " +
                playingTrack.getBoldFormattedTitle()))
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("", "Vote to skip a song. Requires a simple majority. (greater than 50%)")
    }
}