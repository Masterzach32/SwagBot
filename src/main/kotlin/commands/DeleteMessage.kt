package xyz.swagbot.commands

import discord4j.core.`object`.entity.*
import discord4j.core.`object`.entity.channel.*
import io.facet.discord.*
import io.facet.discord.commands.*
import io.facet.discord.commands.arguments.*
import io.facet.discord.commands.dsl.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactive.*
import xyz.swagbot.extensions.*
import xyz.swagbot.features.permissions.*
import xyz.swagbot.util.*

object DeleteMessage : ChatCommand(
    name = "Delete Message",
    aliases = setOf("delete")
) {

    override fun DSLCommandNode<ChatCommandSource>.register() {
        require { hasBotPermission(PermissionType.DEV) }

        argument("messageId", snowflake()) {
            runs { context ->
                val messageId = context.getSnowflake("messageId")
                val channel = getChannel()

                channel.type().async()

                val messageToDelete = getGuild().channels.asFlow()
                    .filterIsInstance<GuildMessageChannel>()
                    .map { it.getMessageById(messageId).await() }
                    .filterNotNull()
                    .take(1)
                    .firstOrNull()

                val confirmationMessage: Message = if (messageToDelete != null) {
                    val author = messageToDelete.authorAsMember.awaitNullable()
                    if (author != null) {
                        channel.createEmbed(baseTemplate.andThen { spec ->
                            spec.setDescription(messageToDelete.content)
                            spec.setFooter(author.displayName, author.avatarUrl)
                            spec.setTitle("Deleted message: **${messageId.asLong()}**")
                        }).await()
                    } else {
                        channel.createEmbed(baseTemplate.andThen { spec ->
                            spec.setDescription(messageToDelete.content)
                            spec.setTitle("Deleted message: **${messageId.asLong()}**")
                        }).await()
                    }
                } else {
                    channel.createEmbed(errorTemplate.andThen { spec ->
                        spec.setDescription("No message in this server with ID **${messageId.asLong()}**.")
                    }).await()
                }

                BotScope.launch {
                    message.delete().await()
                    messageToDelete?.delete()?.await()
                    delay(5000)
                    confirmationMessage.delete().await()
                }
            }
        }
    }
}
