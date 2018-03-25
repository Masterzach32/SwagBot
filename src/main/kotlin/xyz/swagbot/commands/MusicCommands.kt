package xyz.swagbot.commands

import net.masterzach32.commands4k.Command
import net.masterzach32.commands4k.Permission
import net.masterzach32.commands4k.builder.createCommand
import sx.blah.discord.util.EmbedBuilder
import xyz.swagbot.database.getAudioHandler
import xyz.swagbot.utils.BLUE

/*
 * SwagBot - Created on 3/22/2018
 * Author: Zach Kozar
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at the project root.
 */

/**
 * @author Zach Kozar
 * @version 3/22/2018
 */

val AutoPlayCommand = createCommand("Autoplay") {
    aliases("autoplay", "ap")

    scope { Command.Scope.GUILD }
    botPerm { Permission.DEVELOPER }

    helpText {
        description { "Autoplay music when no more tracks are queued. Music chosen by SwagBot is based on the " +
                "previously chosen tracks and genre of the users currently listening." }
        usage("") { "Enable or disable the autoplay feature." }
    }

    onEvent {
        val embed = EmbedBuilder().withColor(BLUE)
        all {
            val autoplay = event.guild.getAudioHandler().toggleShouldAutoplay()

            if (autoplay) {
                embed.withDesc("Autoplay has now been enabled. If there are no tracks in the queue, SwagBot " +
                        "will start automatically playing tracks.")
                if (event.author.getVoiceStateForGuild(event.guild).channel != null &&
                        event.client.ourUser.getVoiceStateForGuild(event.guild).channel == null)
                    event.author.getVoiceStateForGuild(event.guild).channel.join()
            } else
                embed.withDesc("Autoplay has now been disabled.")

            return@all builder.withEmbed(embed)
        }
    }
}