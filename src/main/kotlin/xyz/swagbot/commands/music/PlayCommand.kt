package xyz.swagbot.commands.music

import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Command
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.EmbedBuilder
import sx.blah.discord.util.RequestBuffer
import xyz.swagbot.api.getVideoFromSearch
import xyz.swagbot.api.music.AudioTrackLoadHandler
import xyz.swagbot.api.music.SilentAudioTrackLoadHandler
import xyz.swagbot.api.music.Spotify
import xyz.swagbot.audioPlayerManager
import xyz.swagbot.commands.getBotLockedMessage
import xyz.swagbot.commands.getWrongArgumentsMessage
import xyz.swagbot.database.getAudioHandler
import xyz.swagbot.database.isBotLocked
import xyz.swagbot.dsl.getConnectedVoiceChannel
import xyz.swagbot.dsl.isOnVoice
import xyz.swagbot.utils.RED
import xyz.swagbot.utils.SPOTIFY_GREEN
import xyz.swagbot.utils.getContent

object PlayCommand : Command("Play", "play", "p", scope = Command.Scope.GUILD) {

    init {
        help.usage["<search query>"] = "Searches YouTube for the best matching video and queues it."
        help.usage["<url>"] = "Queues the specified track, playlist or stream in the server's audio player."
    }

    override fun execute(
            cmdUsed: String,
            args: Array<String>,
            event: MessageReceivedEvent,
            builder: AdvancedMessageBuilder
    ): AdvancedMessageBuilder? {

        if(args.isEmpty())
            return getWrongArgumentsMessage(builder, this, cmdUsed)
        if (event.guild.isBotLocked())
            return getBotLockedMessage(builder)
        event.channel.toggleTypingStatus()

        val handler = event.guild.getAudioHandler()

        if (args[0].contains("https://spotify.com") || args[0].contains("https://www.spotify.com") ||
                args[0].contains("https://open.spotify.com") || args[0].contains("spotify:")) {
            val playlist = Spotify.getPlaylist(args[0])
            val embed = EmbedBuilder().withColor(SPOTIFY_GREEN)
            if (playlist != null) {
                embed.withTitle(":musical_note: | Spotify playlist requested by ${event.author.getDisplayName(event.guild)}")
                embed.withDesc("**[${playlist.name}](${playlist.link})**\n")
                embed.appendDesc("Created by: **${playlist.owner}**\n")
                embed.appendDesc(playlist.description)
                embed.withThumbnail(playlist.icon)

                RequestBuffer.request { builder.withEmbed(embed).build() }

                playlist.tracks
                        .forEach {
                            val video = getVideoFromSearch("${it.name} ${it.artists.first().name}")
                            if (video != null)
                                audioPlayerManager.loadItemOrdered(
                                        handler,
                                        video.getUrl(),
                                        SilentAudioTrackLoadHandler(handler, event.author)
                                )
                        }
            } else {
                return builder.withEmbed(embed.withColor(RED).withDesc("Could not locate a Spotify playlist with that URI."))
            }

            return null
        }

        val identifier = if (args[0].contains("http://") || args[0].contains("https://"))
            args[0]
        else {
            getVideoFromSearch(getContent(args, 0))?.getUrl()
        }

        if (identifier == null)
            return builder.withEmbed(EmbedBuilder().withColor(RED).withDesc(
                    "Sorry, I could not find a video that matched that description. Try refining your search."
            ))

        audioPlayerManager.loadItemOrdered(
                handler,
                identifier,
                AudioTrackLoadHandler(
                        handler,
                        event.author,
                        event.guild,
                        event.message,
                        builder
                )
        )

        if (event.client.ourUser.getVoiceStateForGuild(event.guild).channel == null && event.author.isOnVoice())
            event.author.getConnectedVoiceChannel()!!.join()

        return null
    }
}