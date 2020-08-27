package xyz.swagbot.features.music.commands

import com.mojang.brigadier.arguments.StringArgumentType.*
import discord4j.core.*
import io.facet.discord.commands.*
import io.facet.discord.commands.dsl.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*
import xyz.swagbot.extensions.*
import xyz.swagbot.features.music.*
import xyz.swagbot.util.*

object PlayCommand : ChatCommand(
    name = "Play Track",
    aliases = setOf("play", "p"),
    scope = Scope.GUILD,
    category = "music"
) {

    override fun DSLCommandNode<ChatCommandSource>.register(client: GatewayDiscordClient) {
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

                channel.type().async()

                val musicFeature = client.feature(Music)
                val query = context.getString("url/query").let { query ->
                    if ("http://" !in query && "https://" !in query)
                        "ytsearch:$query"
                    else
                        query
                }
                val track = musicFeature.search(query)

                if (track != null) {
                    val trackScheduler = guild.trackScheduler
                    track.setTrackContext(requester, channel)
                    trackScheduler.queue(track)

                    channel.createEmbed(
                        trackRequestedTemplate(requester.displayName, track, trackScheduler.queueTimeLeft())
                    ).awaitComplete()
                } else {
                    channel.createEmbed(errorTemplate.andThen {
                        it.setDescription("You must be connected to a voice channel before you can use this command!")
                    }).awaitComplete()
                }
            }
        }
    }
}
