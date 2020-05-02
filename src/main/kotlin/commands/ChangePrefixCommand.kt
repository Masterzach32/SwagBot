package xyz.swagbot.commands

import com.mojang.brigadier.*
import com.mojang.brigadier.arguments.StringArgumentType.*
import io.facet.discord.commands.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*
import xyz.swagbot.extensions.*
import xyz.swagbot.features.guilds.*
import xyz.swagbot.features.permissions.*
import xyz.swagbot.util.*

object ChangePrefixCommand : ChatCommand {

    override fun register(dispatcher: CommandDispatcher<ChatCommandSource>) {
        listOf("changeprefix", "prefix", "cp").map { alias ->
            literal(alias).then(argument("newPrefix", word()).requires {
                it.hasBotPermission(PermissionType.ADMIN)
            }.executes { context ->
                val source = context.source
                source.guild.map { guild ->
                    val newPrefix = context.getString("newPrefix")
                    source.message.channel.flatMap { channel ->
                        channel.createEmbed(baseTemplate.andThen {
                            it.setDescription("Command prefix changed to **$newPrefix**")
                        })
                    }.then(source.client.feature(GuildStorage).updateCommandPrefixFor(guild.id, newPrefix))
                }.subscribe().let { 1 }
            })
        }.forEach { dispatcher.register(it) }
    }
}
