package xyz.swagbot.features.permissions

import com.mojang.brigadier.arguments.StringArgumentType.*
import discord4j.rest.util.*
import io.facet.discord.commands.*
import io.facet.discord.commands.dsl.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import xyz.swagbot.extensions.*
import xyz.swagbot.util.*

object ChangePermissionCommand : ChatCommand(
    name = "Change User Permissions",
    aliases = setOf("permission", "perm"),
    scope = Scope.GUILD,
    category = "admin",
    discordPermsRequired = PermissionSet.of(Permission.ADMINISTRATOR),
    usage = commandUsage {
        default("View your current permission level.")
        add("<member>", "View another member's permission level.")
        add("<permission> <members/roles..>", "Add the specified permission to the mentioned members.")
    }
) {

    override fun DSLCommandNode<ChatCommandSource>.register() {
        runs {
            val perm = member.botPermission()
            respondEmbed(baseTemplate.andThen {
                description = "Your permission level is **$perm**"
            })
        }

        argument("user", string()) {
            runs {
                val member = message.allMemberMentions.first()

                val perm = member.botPermission()
                respondEmbed(baseTemplate.andThen {
                    description = "**${member.displayName}** has permission **$perm**"
                })
            }
        }

        PermissionType.values().filter { it != PermissionType.DEV }.map { permission ->
            literal(permission.name) {
                argument("users", greedyString()) {
                    require { hasBotPermission(PermissionType.ADMIN) }

                    runs {
                        val channel = getChannel()

                        launch { channel.type().await() }

                        val membersUpdated = message.allMemberMentions
                            .filter { it.updateBotPermission(permission, member) }
                            .toList()
                            .joinToString(separator = "**, **", prefix = "**", postfix = "**") { it.displayName }

                        channel.sendEmbed(baseTemplate.andThen {
                            description = "Updated permissions for $membersUpdated."
                        })
                    }
                }
            }
        }
    }
}
