package xyz.swagbot.features.music.commands

import com.mojang.brigadier.builder.*
import discord4j.core.*
import io.facet.discord.commands.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*
import reactor.core.publisher.*
import xyz.swagbot.extensions.*
import xyz.swagbot.features.music.*
import xyz.swagbot.util.*
import kotlin.math.*

object VoteSkipCommand : ChatCommand(
    name = "Vote Skip",
    aliases = setOf("voteskip", "vs"),
    scope = Scope.GUILD,
    category = "music"
) {

    override fun register(client: DiscordClient, node: LiteralArgumentBuilder<ChatCommandSource>) {
        node.executesAsync { context ->
            val source = context.source
            source.handlePremium().switchIfEmpty {
                val scheduler = source.client.feature(Music).trackSchedulerFor(source.guildId.get())
                source.channel.flatMap { channel ->
                    source.guild.flatMap { it.ourConnectedVoiceChannel }.flatMap { vc ->
                        source.member.get().voiceState
                            .filterWhen { memberVs ->
                                memberVs.channel.map { it.id == vc.id }
                            }
                            .flatMap { _ ->
                                scheduler.player.playingTrack?.trackContext.toMonoOrEmpty().flatMap { trackContext ->
                                    vc.connectedMembers.collectList().flatMap { members ->
                                        trackContext.addSkipVote(source.member.get().id).toMono().filter { it }
                                            .flatMap {
                                                ((members.size-1)/2.0 - trackContext.skipVoteCount).roundToInt()
                                                    .toMono()
                                                    .filter { it <= 0 }
                                                    .flatMap {
                                                        val track = scheduler.playNext()!!
                                                        channel.createEmbed(baseTemplate.andThen {
                                                            it.setDescription("Skipped track: ${track.info.boldFormattedTitle}")
                                                        })
                                                    }
                                                    .switchIfEmpty {
                                                        channel.createEmbed(baseTemplate.andThen {
                                                            it.setDescription(
                                                                "${trackContext.skipVoteCount}/" +
                                                                        "${((members.size-1)/2.0).roundToInt()}" +
                                                                        " votes to skip."
                                                            )
                                                        })
                                                    }
                                            }.switchIfEmpty {
                                                channel.createEmbed(errorTemplate.andThen {
                                                    it.setDescription("You have already voted to skip this track.")
                                                })
                                            }
                                    }
                                }.switchIfEmpty {
                                    channel.createEmbed(errorTemplate.andThen {
                                        it.setDescription("I'm not currently playing a track!")
                                    })
                                }
                            }
                            .switchIfEmpty {
                                channel.createEmbed(errorTemplate.andThen {
                                    it.setDescription("You must be in ${vc.name} to voteskip a track!")
                                })
                            }
                    }.switchIfEmpty {
                        channel.createEmbed(errorTemplate.andThen {
                            it.setDescription("The bot is currently not in a voice channel!")
                        })
                    }
                }.then()
            }
        }
    }


}
