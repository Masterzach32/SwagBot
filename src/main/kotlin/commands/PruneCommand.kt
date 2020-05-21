package xyz.swagbot.commands

import com.mojang.brigadier.arguments.IntegerArgumentType.*
import com.mojang.brigadier.builder.*
import discord4j.core.*
import discord4j.core.`object`.entity.*
import io.facet.discord.commands.*
import io.facet.discord.commands.extensions.*
import reactor.core.publisher.*
import xyz.swagbot.extensions.*
import xyz.swagbot.features.permissions.*
import xyz.swagbot.util.*
import java.time.*

object PruneCommand : ChatCommand(
    name = "Prune Messages",
    aliases = setOf("prune", "purge"),
    scope = Scope.GUILD
) {

    override fun register(client: DiscordClient, node: LiteralArgumentBuilder<ChatCommandSource>) {
        node.then(argument("numMessages", integer(2, 100)).requires {
            it.hasBotPermission(PermissionType.MOD)
        }.executesAsync { context ->
            val source = context.source
            source.guild.flatMap { guild ->
                val delete = context.getInt("numMessages")
                source.message.channel.cast<GuildMessageChannel>().flatMap { channel ->
                    channel.type().switchIfEmpty {
                        channel.bulkDelete(
                            channel.getMessagesBefore(source.message.id)
                                .take(delete.toLong())
                                .map { it.id }
                        ).collectList().flatMap { notDeleted ->
                            source.message.delete().switchIfEmpty {
                                channel.createEmbed(baseTemplate.andThen {
                                    it.setDescription("Deleted **${delete - notDeleted.size}** messages")
                                }).delayElement(Duration.ofSeconds(5)).flatMap { it.delete() }
                            }
                        }
                    }
                }
            }
        })
    }
}
