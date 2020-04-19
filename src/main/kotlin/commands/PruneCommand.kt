package xyz.swagbot.commands

import com.mojang.brigadier.*
import com.mojang.brigadier.arguments.IntegerArgumentType.*
import discord4j.core.`object`.entity.*
import io.facet.discord.commands.*
import io.facet.discord.commands.extensions.*
import reactor.core.publisher.*
import xyz.swagbot.features.permissions.*
import xyz.swagbot.util.*
import java.time.*

object PruneCommand : ChatCommand {

    override fun register(dispatcher: CommandDispatcher<ChatCommandSource>) {
        listOf("prune", "purge").map { alias ->
            literal(alias).then(argument("numMessages", integer(2, 100)).requires {
                it.hasBotPermission(PermissionType.MOD)
            }.executes { context ->
                val source = context.source
                source.guild.flatMap { guild ->
                    val delete = context.getInt("numMessages")
                    source.message.channel.cast<GuildMessageChannel>().flatMap { channel ->
                        channel.type().subscribe()

                        channel.bulkDelete(
                            channel.getMessagesBefore(source.message.id)
                                .take(delete.toLong())
                                .map { it.id }
                        ).collectList().flatMap { notDeleted ->
                            source.message.delete().subscribe()

                            channel.createEmbed(baseTemplate.andThen {
                                it.setDescription("Deleted **${delete - notDeleted.size}** messages")
                            }).delayElement(Duration.ofSeconds(5)).flatMap { it.delete() }
                        }
                    }
                }.subscribe().let { 1 }
            })
        }.forEach { dispatcher.register(it) }
    }
}
