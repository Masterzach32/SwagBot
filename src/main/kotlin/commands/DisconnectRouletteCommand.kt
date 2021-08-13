package xyz.swagbot.commands

import io.facet.commands.*
import io.facet.common.*

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
