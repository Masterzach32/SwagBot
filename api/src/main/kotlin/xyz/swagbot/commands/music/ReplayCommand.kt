package xyz.swagbot.commands.music

import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Command
import net.masterzach32.commands4k.RED
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.EmbedBuilder
import xyz.swagbot.commands.getBotLockedMessage
import xyz.swagbot.database.isBotLocked
import xyz.swagbot.database.trackHandler

object ReplayCommand : Command("Replay Track", "replay", scope = Scope.GUILD) {

    init {
        help.usage[""] = "Reset the progress of the current track."
    }

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent,
                         builder: AdvancedMessageBuilder): AdvancedMessageBuilder? {
        if (event.guild.isBotLocked)
            return getBotLockedMessage(builder)
        val embed = EmbedBuilder()

        val playingTrack = event.guild.trackHandler.player.playingTrack
        try {
            if (playingTrack != null && playingTrack.isSeekable)
                playingTrack.position = 0
            else return builder.withEmbed(embed.withColor(RED).withDesc("There is no track to replay!"))
        } catch (t: Throwable) {
            return builder.withEmbed(embed.withColor(RED).withDesc("Could not reset track position: ${t.message}"))
        }
        return null
    }
}