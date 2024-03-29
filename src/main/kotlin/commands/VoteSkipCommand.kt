package xyz.swagbot.commands

import io.facet.commands.GlobalGuildApplicationCommand
import io.facet.commands.GuildSlashCommandContext
import io.facet.commands.acknowledge
import io.facet.commands.applicationCommandRequest
import io.facet.common.await
import io.facet.common.connectedMembers
import io.facet.common.getConnectedVoiceChannel
import io.facet.common.sendFollowupMessage
import kotlinx.coroutines.flow.count
import xyz.swagbot.extensions.context
import xyz.swagbot.extensions.isPremium
import xyz.swagbot.extensions.trackScheduler
import xyz.swagbot.features.music.trackSkippedTemplate
import kotlin.math.roundToInt

object VoteSkipCommand : GlobalGuildApplicationCommand {

    override val request = applicationCommandRequest("voteskip", "Vote to skip the playing track.")

    override suspend fun GuildSlashCommandContext.execute() {
        val guild = getGuild()

        if (!guild.isPremium())
            return event.reply("Music is a premium feature of SwagBot").withEphemeral(true).await()

        val voiceChannel = guild.getConnectedVoiceChannel()
            ?: return event.reply("Cannot vote to skip as there is nothing playing!").withEphemeral(true).await()

        val memberVs = member.voiceState.await()
        if (memberVs.channelId.map { it != voiceChannel.id }.orElse(true))
            return event.reply("You must be in ${voiceChannel.name} to vote skip!").withEphemeral(true).await()

        val trackScheduler = guild.trackScheduler
        val trackToSkip = trackScheduler.player.playingTrack
            ?: return event.reply("Cannot vote to skip as there is nothing playing!").withEphemeral(true).await()

        val successfulVote = trackToSkip.context.addSkipVote(member.id)

        if (!successfulVote)
            return event.reply("You have already voted to skip this track.").withEphemeral(true).await()

        acknowledge()

        val connectedMembers = voiceChannel.connectedMembers.count()
        val voteThreshold = ((connectedMembers - 1) / 2.0 - trackToSkip.context.skipVoteCount)

        if (voteThreshold <= 0) {
            trackScheduler.playNext()
            event.interactionResponse.sendFollowupMessage {
                embed(
                    trackSkippedTemplate(
                        member.displayName,
                        trackToSkip,
                        guild.trackScheduler.player.playingTrack
                    )
                )
            }
        } else {
            val skipCount = trackToSkip.context.skipVoteCount
            val majority = ((connectedMembers - 1) / 2.0).roundToInt()
            event.interactionResponse.createFollowupMessage("$skipCount / $majority votes to skip.").await()
        }
    }
}
