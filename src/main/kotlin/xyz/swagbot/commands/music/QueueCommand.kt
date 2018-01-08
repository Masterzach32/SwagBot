package xyz.swagbot.commands.music

import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Command
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.IUser
import sx.blah.discord.util.EmbedBuilder
import xyz.swagbot.api.music.TrackUserData
import xyz.swagbot.commands.getWrongArgumentsMessage
import xyz.swagbot.database.getAudioHandler
import xyz.swagbot.database.getCommandPrefix
import xyz.swagbot.dsl.getFormattedLength
import xyz.swagbot.dsl.getFormattedPosition
import xyz.swagbot.dsl.getFormattedTitle
import xyz.swagbot.dsl.getTrackUserData
import xyz.swagbot.utils.BLUE
import xyz.swagbot.utils.RED
import xyz.swagbot.utils.getFormattedTime

object QueueCommand : Command("View Track Queue", "queue", scope = Command.Scope.GUILD) {

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent,
                         builder: AdvancedMessageBuilder): AdvancedMessageBuilder {
        val audioHandler = event.guild.getAudioHandler()
        val embed = EmbedBuilder().withColor(BLUE)
        var pageNumber: Int
        if (audioHandler.getQueue().isEmpty() && audioHandler.player.playingTrack == null)
            return builder.withEmbed(embed.withDesc("The queue is empty! Go add some tracks " +
                    "with the ${event.guild.getCommandPrefix()}play or ${event.guild.getCommandPrefix()}search commands!"))
        else if (args.isEmpty())
            pageNumber = 0
        else {
            try {
                pageNumber = args[0].toInt()
                if (pageNumber > audioHandler.getQueue().size / 15 + 1 || pageNumber < 1)
                    pageNumber = 0
                else
                    pageNumber--
            } catch (e: NumberFormatException) {
                return getWrongArgumentsMessage(builder, this, cmdUsed)
            }
        }

        embed.withTitle("SwagBot Track Queue")
        embed.appendField("Currently Playing: ", "${audioHandler.player.playingTrack.getFormattedTitle()} - " +
                "**${audioHandler.player.playingTrack.getFormattedPosition()}**" +
                " / **${audioHandler.player.playingTrack.getFormattedLength()}** " +
                "(${audioHandler.player.playingTrack.getTrackUserData().author.getDisplayName(event.guild)})", false)
        var count = 0L
        audioHandler.getQueue().forEach { count += it.info.length }
        embed.appendField("Songs in Queue: ", "${audioHandler.getQueue().size}", true)
        embed.appendField("Duration:", getFormattedTime((count/1000).toInt()), true)
        embed.appendField("Volume:", "${(audioHandler.player.volume)}.0", true)
        embed.appendField("Page:", "${(pageNumber + 1)} / ${(audioHandler.getQueue().size / 15) + 1}", true)

        var i = pageNumber * 15
        var str = ""
        while (i < audioHandler.getQueue().size && i < (pageNumber + 1) * 15) {
            str += "${i+1}. ${audioHandler.getQueue()[i].getFormattedTitle()} - " +
                    "**${audioHandler.getQueue()[i].getFormattedLength()}** " +
                    "(${audioHandler.getQueue()[i].getTrackUserData().author.getDisplayName(event.guild)})\n"
            i++
        }
        if (str.isEmpty())
            str = "The queue is empty! Go add some tracks with the ${event.guild.getCommandPrefix()}play " +
                    "or ${event.guild.getCommandPrefix()}search commands!"
        embed.withDesc(str)
        return builder.withEmbed(embed)
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("", "Display the first 15 tracks in queue.")
        usage.put("[page number]", "Display the specified queue page.")
    }
}