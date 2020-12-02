package xyz.swagbot.commands

import com.mojang.brigadier.arguments.IntegerArgumentType.*
import discord4j.core.`object`.entity.channel.*
import discord4j.rest.util.*
import io.facet.discord.commands.*
import io.facet.discord.commands.dsl.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*
import kotlinx.coroutines.*
import xyz.swagbot.extensions.*
import xyz.swagbot.features.permissions.*

object Prune : ChatCommand(
        name = "Purge Messages",
        aliases = setOf("prune", "purge"),
        scope = Scope.GUILD,
        category = "moderator",
        discordPermsRequired = PermissionSet.of(Permission.MANAGE_MESSAGES),
        usage = commandUsage {
            add("<number of messages>", "Delete the last x number of messages in this channel.")
        }
) {

    override fun DSLCommandNode<ChatCommandSource>.register() {
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

                val resultMessage = message.reply("Deleted **${numToDelete - notDeleted.size}** messages")

                launch {
                    delay(10_000)
                    message.delete().await()
                    resultMessage.delete().await()
                }
            }
        }
    }
}
