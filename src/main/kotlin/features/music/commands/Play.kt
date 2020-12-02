package xyz.swagbot.features.music.commands

import com.mojang.brigadier.arguments.StringArgumentType.*
import com.sedmelluq.discord.lavaplayer.track.*
import io.facet.core.extensions.*
import io.facet.discord.commands.*
import io.facet.discord.commands.dsl.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*
import kotlinx.coroutines.*
import xyz.swagbot.extensions.*
import xyz.swagbot.features.music.*
import xyz.swagbot.util.*

object Play : ChatCommand(
    name = "Play Track",
    aliases = setOf("play", "p"),
    scope = Scope.GUILD,
    category = "music",
    description = "Play music in the bot from sources like youtube, soundcloud or any internet url.",
    usage = commandUsage {
        add("<url>", "Will attempt to load the track from the specified URL and queue it.")
        add("<query>", "Will search YouTube and play the first result from the query.")
    }
) {

    override fun DSLCommandNode<ChatCommandSource>.register() {
        argument("url/query", greedyString()) {
            runs { context ->
                val guild = getGuild()
                val channel = getGuildChannel()

                if (!isMusicFeatureEnabled()) {
                    message.reply(notPremiumTemplate(prefixUsed))
                    return@runs
                }

                if (member.voiceState.awaitNullable()?.channelId?.value == null) {
                    message.reply("You must be in a voice channel to add music to the queue!")
                    return@runs
                }

                launch { channel.type().await() }

                val musicFeature = client.feature(Music)
                val query = context.getString("url/query")
                val item: AudioItem? = try {
                    if ("http://" in query || "https://" in query)
                        musicFeature.loadItem(query)
                    else
                        musicFeature.loadItem("ytsearch:$query")
                } catch (e: Throwable) {
                    message.reply("I couldn't find anything for *\"$query\"*.")
                    return@runs
                }

                val scheduler = guild.trackScheduler
                when (item) {
                    is AudioTrack -> {
                        item.setTrackContext(member, channel)
                        scheduler.queue(item)

                        message.reply(
                            trackRequestedTemplate(
                                member.displayName,
                                item,
                                scheduler.queueTimeLeft
                            )
                        )
                        if (getGuild().getOurConnectedVoiceChannel() == null)
                            member.getConnectedVoiceChannel()?.join(this)
                    }
                    is AudioPlaylist -> {
                        if (item.isSearchResult) {
                            val track = item.tracks.maxByOrNull { track ->
                                track.info.title.toLowerCase().let { "audio" in it || "lyrics" in it }
                            }

                            if (track != null) {
                                track.setTrackContext(member, channel)
                                scheduler.queue(track)

                                message.reply(
                                    trackRequestedTemplate(
                                        member.displayName,
                                        track,
                                        scheduler.queueTimeLeft
                                    )
                                )
                                if (getGuild().getOurConnectedVoiceChannel() == null)
                                    member.getConnectedVoiceChannel()?.join(this)
                            } else
                                message.reply("I couldn't find anything for *\"$query\"*.")
                        } else {
                            item.tracks.forEach { track ->
                                track.setTrackContext(member, channel)
                                scheduler.queue(track)
                            }

                            message.reply(baseTemplate.andThen {
                                title = ":musical_note: | Playlist requested by ${member.displayName}"
                                description = """
                                    **${item.name}**
                                    ${item.tracks.size} Tracks
                                """.trimIndent().trim()
                            })
                            if (getGuild().getOurConnectedVoiceChannel() == null)
                                member.getConnectedVoiceChannel()?.join(this)
                        }
                    }
                    else -> message.reply("I couldn't find anything for *\"$query\"*.")
                }
            }
        }
    }
}
