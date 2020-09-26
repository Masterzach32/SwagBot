package xyz.swagbot.features.music.commands

import io.facet.discord.commands.*
import io.facet.discord.commands.dsl.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*
import xyz.swagbot.extensions.*
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
            val member = member!!

            if (!isMusicFeatureEnabled())
                return@runs channel.createEmbed(notPremiumTemplate(prefixUsed)).awaitComplete()

            val voiceChannel = guild.getOurConnectedVoiceChannel()
                ?: return@runs channel.createEmbed(errorTemplate.andThen {
                    it.setDescription("The bot is currently not in a voice channel!")
                }).awaitComplete()

            val memberVs = member.voiceState.await()
            if (memberVs.channelId.map { it != voiceChannel.id }.orElse(true))
                return@runs channel.createEmbed(errorTemplate.andThen {
                    it.setDescription("You must be in ${voiceChannel.name} to vote skip a track!")
                }).awaitComplete()

            val trackScheduler = guild.trackScheduler
            val trackToSkip = trackScheduler.player.playingTrack
                ?: return@runs channel.createEmbed(errorTemplate.andThen {
                    it.setDescription("I'm not currently playing a track!")
                }).awaitComplete()

            val successfulVote = trackToSkip.context.addSkipVote(member.id)

            if (!successfulVote)
                return@runs channel.createEmbed(errorTemplate.andThen {
                    it.setDescription("You have already voted to skip this track.")
                }).awaitComplete()

            val connectedMemberIds = voiceChannel.getConnectedMemberIds()
            val voteThreshold = ((connectedMemberIds.size-1)/2.0 - trackToSkip.context.skipVoteCount)

            if (voteThreshold <= 0) {
                trackScheduler.playNext()
                channel.createEmbed(baseTemplate.andThen {
                    it.setDescription("Skipped track: ${trackToSkip.info.boldFormattedTitle}")
                }).awaitComplete()
            } else {
                channel.createEmbed(baseTemplate.andThen {
                    it.setDescription(
                        "${trackToSkip.context.skipVoteCount}/${((connectedMemberIds.size-1)/2.0).roundToInt()} " +
                                "votes to skip."
                    )
                }).awaitComplete()
            }
        }
    }


}
