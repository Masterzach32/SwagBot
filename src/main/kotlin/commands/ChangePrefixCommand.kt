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

                if (newPrefix.length in 1..10) {
                    getGuild().updateCommandPrefix(newPrefix)
                    message.reply("Command prefix changed to **$newPrefix**")
                } else
                    message.reply("Command prefixes must be between 1 and 10 characters.")
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
