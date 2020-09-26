package xyz.swagbot.features.music.commands

import com.mojang.brigadier.arguments.StringArgumentType.*
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
                val channel = getChannel()

                if (!isMusicFeatureEnabled())
                    return@runs channel.createEmbed(notPremiumTemplate(prefixUsed)).awaitComplete()

                val requester = member!! // requester is not null as play is only run in guild

                if (!requester.voiceState.await().channelId.isPresent)
                    return@runs channel.createEmbed(errorTemplate.andThen {
                        it.setDescription("You must be in a voice channel to add music to the queue!")
                    }).awaitComplete()

                launch { channel.type().await() }

                val musicFeature = client.feature(Music)
                val query = context.getString("url/query").let { query ->
                    if ("http://" !in query && "https://" !in query)
                        "ytsearch:$query"
                    else
                        query
                }
                val track = musicFeature.search(query, Music.SearchResultPolicy.Limited(10))
                    .maxByOrNull { track -> track.info.title.toLowerCase().let { "audio" in it || "lyrics" in it } }

                if (track != null) {
                    val trackScheduler = guild.trackScheduler
                    track.setTrackContext(requester, channel)
                    trackScheduler.queue(track)

                    channel.createEmbed(
                        trackRequestedTemplate(requester.displayName, track, trackScheduler.queueTimeLeft())
                    ).awaitComplete()
                } else {
                    channel.createEmbed(errorTemplate.andThen {
                        it.setDescription("I couldn't find anything for *\"${context.getString("url/query")}\"*.")
                    }).awaitComplete()
                }
            }
        }
    }
}
