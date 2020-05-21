package xyz.swagbot.commands

import com.mojang.brigadier.*
import com.mojang.brigadier.arguments.*
import com.mojang.brigadier.arguments.StringArgumentType.*
import com.mojang.brigadier.builder.*
import discord4j.core.*
import io.facet.discord.commands.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*
import xyz.swagbot.*
import xyz.swagbot.extensions.*
import xyz.swagbot.features.guilds.*
import xyz.swagbot.features.permissions.*
import xyz.swagbot.util.*

object ChangePrefixCommand : ChatCommand(
    name = "Change Prefix",
    aliases = setOf("changeprefix", "prefix", "cp"),
    scope = Scope.GUILD
) {

    override fun register(client: DiscordClient, node: LiteralArgumentBuilder<ChatCommandSource>) {
        node.then(argument("newPrefix", commandPrefix()).requires {
            it.hasBotPermission(PermissionType.ADMIN)
        }.executesAsync { context ->
            val source = context.source
            source.guild.flatMap { guild ->
                val newPrefix = context.getString("newPrefix")
                source.message.channel.flatMap { channel ->
                    channel.createEmbed(baseTemplate.andThen {
                        it.setDescription("Command prefix changed to **$newPrefix**")
                    })
                }.then(source.client.feature(GuildStorage).updateCommandPrefixFor(guild.id, newPrefix))
            }
        })
    }

    private fun commandPrefix() = CommandPrefixArgumentType

    object CommandPrefixArgumentType : ArgumentType<String> {

        override fun parse(reader: StringReader): String {
            var cp = ""
            while (reader.remainingLength > 0 && cp.length < 10) {
                val next = reader.peek()
                if (next != ' ' && next != '\t' && next != '\n')
                    cp += reader.read()
                else
                    return cp
            }
            return cp
        }

        override fun getExamples(): MutableCollection<String> {
            return mutableListOf("~", "_", "!swagbot!")
        }
    }
}
