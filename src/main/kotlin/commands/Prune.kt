package xyz.swagbot.commands

import discord4j.common.util.*
import discord4j.core.`object`.entity.*
import discord4j.rest.util.*
import io.facet.discord.appcommands.*
import io.facet.discord.extensions.*
import kotlinx.coroutines.flow.*

object Prune : GlobalGuildApplicationCommand, PermissibleApplicationCommand {

    override val request = applicationCommandRequest("prune", "Delete the last X number of messages in this text channel") {
        addOption("count", "Number of messages to delete", ApplicationCommandOptionType.INTEGER, true)
    }

    override suspend fun hasPermission(user: User, guild: Guild?): Boolean = user.id == user.client.applicationInfo.await().ownerId

    override suspend fun GuildSlashCommandContext.execute() {
        val count: Long by options
        val channel = getChannel()

        if (count !in 2..100)
            return event.replyEphemeral("`count` must be between 2 and 100.").await()

        event.acknowledgeEphemeral().await()

        val notDeleted: List<Snowflake> = channel.bulkDelete(
            channel.getMessagesBefore(event.interaction.id)
                .take(count)
                .map { it.id }
        ).await()

        val stillNotDeleted = notDeleted.asFlow()
            .map { client.getMessageById(channel.id, it).await() }
            .buffer()
            .map { it?.delete()?.await() }
            .count()

        event.interactionResponse
            .createFollowupMessage("Deleted **${count - stillNotDeleted}** messages.")
            .await()
    }
}
