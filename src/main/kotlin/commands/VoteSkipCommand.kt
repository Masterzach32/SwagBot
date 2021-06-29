package xyz.swagbot.commands

import io.facet.discord.appcommands.*
import io.facet.discord.appcommands.extensions.*
import io.facet.discord.extensions.*
import kotlinx.coroutines.flow.*
import xyz.swagbot.extensions.*
import xyz.swagbot.features.music.*
import kotlin.math.*

object VoteSkipCommand : GlobalGuildApplicationCommand {

    override val request = applicationCommandRequest("voteskip", "Vote to skip the playing track.")

    override suspend fun GuildSlashCommandContext.execute() {
        val guild = getGuild()

        if (!guild.isPremium())
            return event.replyEphemeral("Music is a premium feature of SwagBot").await()

        val voiceChannel = guild.getConnectedVoiceChannel()
            ?: return event.replyEphemeral("Cannot vote to skip as there is nothing playing!").await()

        val memberVs = member.voiceState.await()
        if (memberVs.channelId.map { it != voiceChannel.id }.orElse(true))
            return event.replyEphemeral("You must be in ${voiceChannel.name} to vote skip!").await()

        val trackScheduler = guild.trackScheduler
        val trackToSkip = trackScheduler.player.playingTrack
            ?: return event.replyEphemeral("Cannot vote to skip as there is nothing playing!").await()

        val successfulVote = trackToSkip.context.addSkipVote(member.id)

        if (!successfulVote)
            return event.replyEphemeral("You have already voted to skip this track.").await()

        acknowledge()

        val connectedMembers = voiceChannel.connectedMembers.count()
        val voteThreshold = ((connectedMembers - 1) / 2.0 - trackToSkip.context.skipVoteCount)

        if (voteThreshold <= 0) {
            trackScheduler.playNext()
            createFollowupMessage(
                trackSkippedTemplate(
                    member.displayName,
                    trackToSkip,
                    guild.trackScheduler.player.playingTrack
                )
            )
        } else {
            val skipCount = trackToSkip.context.skipVoteCount
            val majority = ((connectedMembers - 1) / 2.0).roundToInt()
            event.interactionResponse.createFollowupMessage("$skipCount / $majority votes to skip.").await()
        }
    }
}
