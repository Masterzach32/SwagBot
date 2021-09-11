package xyz.swagbot.commands

import io.facet.commands.GlobalGuildApplicationCommand
import io.facet.commands.GuildSlashCommandContext
import io.facet.commands.acknowledge
import io.facet.commands.applicationCommandRequest
import io.facet.common.await
import io.facet.common.getConnectedMembers
import io.facet.common.getConnectedVoiceChannel
import io.facet.common.sendFollowupMessage

object DisconnectRouletteCommand : GlobalGuildApplicationCommand {

    override val request = applicationCommandRequest("droulette", "Disconnect a random user from your voice channel.")

    override suspend fun GuildSlashCommandContext.execute() {
        acknowledge()
        val connectedMembers = member.getConnectedVoiceChannel()?.getConnectedMembers() ?: return

        connectedMembers.random().let { member ->
            member.edit()
                .withNewVoiceChannelOrNull(null)
                .await()

            event.interactionResponse.sendFollowupMessage {
                content = "The roulette has chosen **${member.displayName}**!"
            }
        }
    }
}
