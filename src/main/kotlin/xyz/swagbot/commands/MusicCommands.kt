package xyz.swagbot.commands

import com.vdurmont.emoji.EmojiParser
import net.masterzach32.commands4k.Permission
import net.masterzach32.commands4k.builder.createCommand
import sx.blah.discord.util.EmbedBuilder
import xyz.swagbot.api.music.TrackHandler
import xyz.swagbot.commands.music.NowPlayingCommand
import xyz.swagbot.database.getAudioHandler
import xyz.swagbot.database.getCommandPrefix
import xyz.swagbot.database.refreshAudioPlayer
import xyz.swagbot.dsl.getTrackUserData
import xyz.swagbot.utils.BLUE
import xyz.swagbot.utils.RED
import xyz.swagbot.utils.listOfEmojis

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

val NowPlayingCommand2 = createCommand("Now Playing") {
    aliases = listOf("np2")

    helpText {
        description = "Display the currently playing song with some music controls"
    }
}

val QueueCommand2 = createCommand("View Track Queue") {
    aliases = listOf("queue2")

    helpText {
        description = "View all tracks in the queue."
    }

    val reactions = listOfEmojis("arrow_backward", "stop_button", "arrow_forward")

    onEvent {
        guild {
            val embed = EmbedBuilder().withColor(BLUE)
            if (args.isNotEmpty() && (args[0].contains("youtu") || args[0].contains("soundcloud")))
                embed.withColor(RED).withDesc(("`~$cmdUsed` is used to view queued tracks. Use `~play` or `~search` to add a video or song to the queue.").replace("~", event.guild.getCommandPrefix())
                )
            else {
                val trackHandler = event.guild.getAudioHandler()
                val browser = trackHandler.getQueueBrowser()

                if (browser.isEmpty())
                    embed.withDesc(("The queue is empty! Go add some tracks with the ~play or ~search commands!").replace("~", event.guild.getCommandPrefix()))

            }

            return@guild builder.withEmbed(embed)
        }
    }
}