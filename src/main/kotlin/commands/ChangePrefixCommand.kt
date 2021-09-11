package xyz.swagbot.commands

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import discord4j.rest.util.Permission
import discord4j.rest.util.PermissionSet
import io.facet.chatcommands.*
import io.facet.common.reply
import xyz.swagbot.extensions.hasBotPermission
import xyz.swagbot.extensions.updateCommandPrefix
import xyz.swagbot.features.permissions.PermissionType

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
