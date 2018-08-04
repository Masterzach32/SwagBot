package xyz.swagbot.commands.music

import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Command
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.EmbedBuilder
import xyz.swagbot.commands.getBotLockedMessage
import xyz.swagbot.database.commandPrefix
import xyz.swagbot.database.isBotLocked
import xyz.swagbot.database.trackHandler
import xyz.swagbot.dsl.*
import xyz.swagbot.utils.BLUE
import xyz.swagbot.utils.RED

/*
 * SwagBot - Created on 1/8/2018
 * Author: Zach Kozar
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at the project root.
 */

/**
 * @author Zach Kozar
 * @version 1/8/2018
 */
object PauseResumeCommand : Command("Pause / Resume", "pause", "unpause", "resume", scope = Scope.GUILD) {

    init {
        help.desc = "Pause or resume the bot's music."
    }

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent,
                         builder: AdvancedMessageBuilder): AdvancedMessageBuilder {
        if (event.guild.isBotLocked)
            return getBotLockedMessage(builder)
        val embed = EmbedBuilder().withColor(RED)
        val player = event.guild.trackHandler.player

        if (player.playingTrack == null)
            return builder.withEmbed(embed.withDesc("Im not playing anything right now. Go add some tracks with" +
                    " the ~play or ~search commands!".replace("~", event.guild.commandPrefix)))

        if (cmdUsed == "pause") {
            if (player.isPaused)
                return builder.withEmbed(embed.withDesc("Currently playing track is already paused."))
            player.isPaused = true
            return builder.withEmbed(embed.withColor(BLUE).withDesc("Paused " +
                    "${player.playingTrack.getBoldFormattedTitle()} at **${player.playingTrack.getFormattedPosition()}**" +
                    " / **${player.playingTrack.getFormattedLength()}**"))
        }
        if (!player.isPaused)
            return builder.withEmbed(embed.withDesc("Currently playing track is not paused."))
        player.isPaused = false
        return builder.withEmbed(embed.withColor(BLUE).withDesc("Resumed " +
                player.playingTrack.getBoldFormattedTitle()))
    }
}