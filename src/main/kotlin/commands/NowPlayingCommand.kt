package xyz.swagbot.commands

import com.sedmelluq.discord.lavaplayer.track.*
import discord4j.core.`object`.entity.*
import io.facet.discord.commands.*
import io.facet.discord.commands.dsl.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*
import xyz.swagbot.extensions.*
import xyz.swagbot.features.music.*
import xyz.swagbot.util.*

object NowPlayingCommand : ChatCommand(
    name = "Now Playing",
    aliases = setOf("nowplaying", "playing", "np"),
    scope = Scope.GUILD,
    category = "music"
) {

    override fun DSLCommandNode<ChatCommandSource>.register() {
        runs { context ->
            if (!isMusicFeatureEnabled()) {
                message.reply(notPremiumTemplate(prefixUsed))
                return@runs
            }

            val musicFeature = client.feature(Music)
            val playingTrack: AudioTrack? = musicFeature.trackSchedulerFor(guildId!!).player.playingTrack

            if (playingTrack != null) {
                val requester = client
                    .getMemberById(guildId!!, playingTrack.context.requesterId)
                    .awaitNullable()
                val volume = musicFeature.getVolumeFor(guildId!!)
                message.reply(baseTemplate.andThen {
                    title = ":musical_note: | Now Playing"

                    description = "${playingTrack.info.boldFormattedTitleWithLink} - " +
                        "**${playingTrack.formattedPosition}** / **${playingTrack.formattedLength}**" +
                        "\nAuthor/Channel: **${playingTrack.info.author}**" +
                        "\nRequested by: **${requester?.displayName ?: "Unknown"}**" +
                        "\nVolume: **${volume}/100**"

                    if (playingTrack.info.thumbnailUrl != null)
                        thumbnailUrl = playingTrack.info.thumbnailUrl!!
                })
            } else {
                message.reply(
                    "I'm not playing anything right now. Go add some music with the `~play` or `~search` commands!"
                        .replace("~", prefixUsed)
                )
            }
        }
    }
}
