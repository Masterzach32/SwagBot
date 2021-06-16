package xyz.swagbot.commands

import com.mojang.brigadier.arguments.IntegerArgumentType.*
import discord4j.common.util.*
import discord4j.core.`object`.entity.*
import discord4j.core.`object`.entity.channel.*
import discord4j.rest.util.*
import io.facet.discord.appcommands.*
import io.facet.discord.commands.*
import io.facet.discord.commands.dsl.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import xyz.swagbot.extensions.*
import xyz.swagbot.features.permissions.*

object Prune : GlobalGuildApplicationCommand, PermissibleApplicationCommand {

    override val request = applicationCommandRequest("prune", "Delete the last X number of messages in this text channel") {
        addOption("count", "Number of messages to delete", ApplicationCommandOptionType.INTEGER, true)
    }

    override suspend fun hasPermission(user: User, guild: Guild?): Boolean = user.id == user.client.applicationInfo.await().ownerId

    override suspend fun GuildInteractionContext.execute() {
        val numToDelete = command.getOption("count").get().value.get().asLong()
        val channel = getChannel()

        if (numToDelete !in 2..100)
            return event.replyEphemeral("`count` must be between 2 and 100.").await()

        event.acknowledgeEphemeral().await()

        val notDeleted: List<Snowflake> = channel.bulkDelete(
            channel.getMessagesBefore(event.interaction.id)
                .take(numToDelete)
                .map { it.id }
        ).await()

        val stillNotDeleted = notDeleted.asFlow()
            .map { client.getMessageById(channel.id, it).await() }
            .buffer()
            .map { it?.delete()?.await() }
            .count()

        event.interactionResponse
            .createFollowupMessage("Deleted **${numToDelete - stillNotDeleted}** messages.")
            .await()

//        GlobalScope.launch {
//            delay(10_000)
//            client.getMessageById(channel.id, messageData.id().asLong().toSnowflake()).await().delete().await()
//        }
    }

    fun DSLCommandNode<ChatCommandSource>.register() {
        argument("numMessages", integer(2, 100)) {
            require { hasBotPermission(PermissionType.MOD) }

            runs { context ->
                val numToDelete = context.getInt("numMessages").toLong()
                val channel = event.message.channel.await() as GuildMessageChannel
                launch { channel.type().await() }

                val notDeleted = channel.bulkDelete(
                    channel.getMessagesBefore(event.message.id)
                        .take(numToDelete)
                        .map { it.id }
                ).await()

                val stillNotDeleted = notDeleted.asFlow()
                    .map { client.getMessageById(channel.id, it).await() }
                    .buffer()
                    .map { it?.delete("")?.await() }
                    .count()

                val resultMessage = message.reply("Deleted **${numToDelete - stillNotDeleted}** messages")

                launch {
                    delay(10_000)
                    message.delete().await()
                    resultMessage.delete().await()
                }
            }
        }
    }
}
