package xyz.swagbot.features.music.commands

import io.facet.discord.commands.*
import io.facet.discord.commands.dsl.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*
import kotlinx.coroutines.flow.*
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
            val guild = getGuild()

            if (!isMusicFeatureEnabled()) {
                message.reply(notPremiumTemplate(prefixUsed))
                return@runs
            }

            val voiceChannel = guild.getOurConnectedVoiceChannel() ?: return@runs

            val memberVs = member.voiceState.await()
            if (memberVs.channelId.map { it != voiceChannel.id }.orElse(true)) {
                message.reply("You must be in ${voiceChannel.name} to vote skip a track!")
                return@runs
            }

            val trackScheduler = guild.trackScheduler
            val trackToSkip = trackScheduler.player.playingTrack
            if (trackToSkip == null) {
                message.reply("Cannot vote to skip as there is no track playing!")
                return@runs
            }

            val successfulVote = trackToSkip.context.addSkipVote(member.id)

            if (!successfulVote) {
                message.reply("You have already voted to skip this track.")
                return@runs
            }

            val connectedMembers = voiceChannel.connectedMembers.count()
            val voteThreshold = ((connectedMembers - 1) / 2.0 - trackToSkip.context.skipVoteCount)

            if (voteThreshold <= 0) {
                trackScheduler.playNext()
                message.reply(
                    trackSkippedTemplate(
                        member.displayName,
                        trackToSkip,
                        guild.trackScheduler.player.playingTrack
                    )
                )
            } else {
                val skipCount = trackToSkip.context.skipVoteCount
                val majority = ((connectedMembers - 1) / 2.0).roundToInt()
                message.reply("$skipCount / $majority votes to skip.")
            }
        }
    }


}
