package xyz.swagbot.commands

import net.masterzach32.commands4k.Command
import net.masterzach32.commands4k.Permission
import net.masterzach32.commands4k.builder.createCommand
import sx.blah.discord.util.EmbedBuilder
import xyz.swagbot.database.getAudioHandler
import xyz.swagbot.database.refreshAudioPlayer
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
    aliases = listOf("autoplay", "ap")
    
    botPerm = Permission.DEVELOPER

    helpText {
        description = "Autoplay music when no more tracks are queued. Music chosen by SwagBot is based on the " +
                "previously chosen tracks and genre of the users currently listening."
        usage[""] = "Enable or disable the autoplay feature."
    }

    onEvent {
        guild {
            val embed = EmbedBuilder().withColor(BLUE)
            val autoplay = event.guild.getAudioHandler().toggleShouldAutoplay()

            if (autoplay) {
                embed.withDesc("Autoplay has now been enabled. If there are no tracks in the queue, SwagBot " +
                        "will start automatically playing tracks.")
                if (event.author.getVoiceStateForGuild(event.guild).channel != null &&
                        event.client.ourUser.getVoiceStateForGuild(event.guild).channel == null)
                    event.author.getVoiceStateForGuild(event.guild).channel.join()
            } else
                embed.withDesc("Autoplay has now been disabled.")

            return@guild builder.withEmbed(embed)
        }
    }
}

val RefreshAudioPlayerCommand = createCommand("Refresh Audio Player") {
    aliases = listOf("refresh")

    botPerm = Permission.MOD

    helpText {
        description = "Refresh the audio player if it becomes unresponsive. Clears the queue and re-initializes " +
                "the server's audio player."
    }

    onEvent {
        guild {
            event.guild.refreshAudioPlayer()

            return@guild null
        }
    }
}