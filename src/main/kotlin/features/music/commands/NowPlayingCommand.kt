package xyz.swagbot.features.music.commands

import com.sedmelluq.discord.lavaplayer.track.*
import discord4j.core.*
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

    override fun DSLCommandNode<ChatCommandSource>.register(client: GatewayDiscordClient) {
        runs { context ->
            val channel = getChannel()
            val musicFeature = client.feature(Music)
            val playingTrack: AudioTrack? = musicFeature.trackSchedulerFor(guildId!!).player.playingTrack

            if (playingTrack != null) {
                val requester = client
                    .getMemberById(guildId!!, playingTrack.context.requesterId)
                    .awaitNullable()
                val volume = musicFeature.volumeFor(guildId!!)
                channel.createEmbed(baseTemplate.andThen {
                    it.setTitle(":musical_note: | Now Playing")

                    val desc = "${playingTrack.info.boldFormattedTitleWithLink} - " +
                            "**${playingTrack.formattedPosition}** / **${playingTrack.formattedLength}**" +
                            "\nAuthor/Channel: **${playingTrack.info.author}**" +
                            "\nRequested by: **${requester?.displayName ?: "Unknown"}**" +
                            "\nVolume: **${volume}/100**"
                    it.setDescription(desc)

                    if (playingTrack.info.hasThumbnail)
                        it.setThumbnail(playingTrack.info.thumbnailUrl)
                }).await()
            } else {
                channel.createEmbed(errorTemplate.andThen {
                    it.setDescription(
                        "Im not playing anything right now. Go add some music with the `~play` or `~search` commands!"
                            .replace("~", prefixUsed)
                    )
                }).await()
            }
        }
    }
}
