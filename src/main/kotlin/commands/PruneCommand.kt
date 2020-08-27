package xyz.swagbot.commands

import com.mojang.brigadier.arguments.IntegerArgumentType.*
import discord4j.core.*
import discord4j.core.`object`.entity.channel.*
import discord4j.rest.util.*
import io.facet.discord.commands.*
import io.facet.discord.commands.dsl.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*
import kotlinx.coroutines.*
import xyz.swagbot.extensions.*
import xyz.swagbot.features.permissions.*
import xyz.swagbot.util.*

object PruneCommand : ChatCommand(
    name = "Prune Messages",
    aliases = setOf("prune", "purge"),
    scope = Scope.GUILD,
    category = "moderator",
    discordPermsRequired = PermissionSet.of(Permission.MANAGE_MESSAGES)
) {

    override fun DSLCommandNode<ChatCommandSource>.register(client: GatewayDiscordClient) {
        argument("numMessages", integer(2, 100)) {
            require { hasBotPermission(PermissionType.MOD) }

            runs { context ->
                val numToDelete = context.getInt("numMessages").toLong()
                val channel = event.message.channel.await() as GuildMessageChannel
                channel.type().async()

                val notDeleted = channel.bulkDelete(
                    channel.getMessagesBefore(event.message.id)
                        .take(numToDelete)
                        .map { it.id }
                ).await()

                message.delete().async()

                val resultMessage = channel.createEmbed(baseTemplate.andThen {
                    it.setDescription("Deleted **${numToDelete - notDeleted.size}** messages")
                }).await()

                delay(5000)

                resultMessage.delete().async()
            }
        }
    }
}
