package xyz.swagbot.commands

import com.mojang.brigadier.*
import com.mojang.brigadier.arguments.StringArgumentType.*
import io.facet.discord.commands.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*
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
                source.guildId.ifPresent { guildId ->
                    val newPrefix = context.getString("newPrefix")
                    source.client.feature(GuildStorage).updateCommandPrefixFor(guildId, newPrefix)
                    source.message.channel.flatMap {
                        it.createEmbed(baseTemplate.andThen {
                            it.setDescription("Command prefix changed to **$newPrefix**")
                        })
                    }.subscribe()
                }.let { 1 }
            })
        }.forEach { dispatcher.register(it) }
    }
}
