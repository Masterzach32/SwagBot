package xyz.swagbot.commands

import com.mojang.brigadier.*
import com.mojang.brigadier.arguments.*
import discord4j.rest.util.*
import io.facet.discord.commands.*
import io.facet.discord.commands.dsl.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*
import xyz.swagbot.extensions.*
import xyz.swagbot.features.permissions.*
import xyz.swagbot.util.*

object ChangePrefixCommand : ChatCommand(
    name = "Change Prefix",
    aliases = setOf("changeprefix", "prefix", "cp"),
    scope = Scope.GUILD,
    category = "admin",
    discordPermsRequired = PermissionSet.of(Permission.ADMINISTRATOR)
) {

    override fun DSLCommandNode<ChatCommandSource>.register() {
        argument("newPrefix", CommandPrefixArgumentType) {
            require {
                hasBotPermission(PermissionType.ADMIN)
            }

            runs { context ->
                val newPrefix = context.getString("newPrefix")

                getGuild().updateCommandPrefix(newPrefix)

                event.message.channel.await().createEmbed(baseTemplate.andThen {
                    it.setDescription("Command prefix changed to **$newPrefix**")
                }).awaitComplete()
            }
        }
    }

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
            return mutableListOf("~", "_", "swagbot!")
        }
    }
}
