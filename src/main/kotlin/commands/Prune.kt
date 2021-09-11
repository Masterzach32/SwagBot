package xyz.swagbot.commands

import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.User
import io.facet.commands.GlobalGuildApplicationCommand
import io.facet.commands.GuildSlashCommandContext
import io.facet.commands.PermissibleApplicationCommand
import io.facet.commands.applicationCommandRequest
import io.facet.common.await
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.map

object Prune : GlobalGuildApplicationCommand, PermissibleApplicationCommand {

    override val request =
        applicationCommandRequest("prune", "Delete the last X number of messages in this text channel") {
            int("count", "Number of messages to delete", true)
        }

    override suspend fun hasPermission(user: User, guild: Guild?): Boolean =
        user.id == user.client.applicationInfo.await().ownerId

    override suspend fun GuildSlashCommandContext.execute() {
        val count: Long by options
        val channel = getChannel()

        if (count !in 2..100)
            return event.reply("`count` must be between 2 and 100.").withEphemeral(true).await()

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
