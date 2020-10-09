package xyz.swagbot.features.music.commands

import io.facet.discord.commands.*
import io.facet.discord.commands.dsl.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*
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

    override fun DSLCommandNode<ChatCommandSource>.register() {
        runs { context ->
            val channel = getChannel()
            val guild = getGuild()

            if (!isMusicFeatureEnabled()) {
                respondEmbed(notPremiumTemplate(prefixUsed))
                return@runs
            }

            val voiceChannel = guild.getOurConnectedVoiceChannel()
            if (voiceChannel == null) {
                respondEmbed(errorTemplate.andThen {
                    description = "The bot is currently not in a voice channel!"
                })
                return@runs
            }

            val memberVs = member.voiceState.await()
            if (memberVs.channelId.map { it != voiceChannel.id }.orElse(true)) {
                respondEmbed(errorTemplate.andThen {
                    description = "You must be in ${voiceChannel.name} to vote skip a track!"
                })
                return@runs
            }

            val trackScheduler = guild.trackScheduler
            val trackToSkip = trackScheduler.player.playingTrack
            if (trackToSkip == null) {
                respondEmbed(errorTemplate.andThen {
                    description = "I'm not currently playing a track!"
                })
                return@runs
            }

            val successfulVote = trackToSkip.context.addSkipVote(member.id)

            if (!successfulVote) {
                channel.createEmbed(errorTemplate.andThen {
                    description = "You have already voted to skip this track."
                })
                return@runs
            }

            val connectedMemberIds = voiceChannel.getConnectedMemberIds()
            val voteThreshold = ((connectedMemberIds.size-1)/2.0 - trackToSkip.context.skipVoteCount)

            if (voteThreshold <= 0) {
                trackScheduler.playNext()
                respondEmbed(baseTemplate.andThen {
                    description = "Skipped track: ${trackToSkip.info.boldFormattedTitle}"
                })
            } else {
                val skipCount = trackToSkip.context.skipVoteCount
                val majority = ((connectedMemberIds.size-1)/2.0).roundToInt()
                respondEmbed(baseTemplate.andThen {
                    description = "$skipCount / $majority votes to skip."
                })
            }
        }
    }


}
