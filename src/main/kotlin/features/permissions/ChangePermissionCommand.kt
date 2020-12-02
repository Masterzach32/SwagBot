package xyz.swagbot.features.permissions

import com.mojang.brigadier.arguments.StringArgumentType.*
import discord4j.rest.util.*
import io.facet.discord.commands.*
import io.facet.discord.commands.dsl.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.jetbrains.exposed.sql.*
import xyz.swagbot.extensions.*
import xyz.swagbot.features.system.*
import xyz.swagbot.util.*

object ChangePermissionCommand : ChatCommand(
    name = "Change User Permissions",
    aliases = setOf("permission", "perm"),
    scope = Scope.GUILD,
    category = "admin",
    usage = commandUsage {
        default("View your current permission level.")
        add("<member>", "View another member's permission level.")
        add("<permission> <members/roles..>", "Add the specified permission to the mentioned members.")
    }
) {

    override fun DSLCommandNode<ChatCommandSource>.register() {
        runs {
            val perm = member.botPermission()
            message.reply("Your permission level is **$perm**")
        }

        argument("user", string()) {
            require { hasBotPermission(PermissionType.MOD) }

            runs {
                val member = message.allMemberMentions.first()

                val perm = member.botPermission()
                message.reply("**${member.displayName}** has permission **$perm**")
            }
        }

        literal("assigned") {
            require {
                hasBotPermission(PermissionType.ADMIN) &&
                    member.basePermissions.await().contains(Permission.ADMINISTRATOR)
            }

            runs {
                val permsAssigned = sql {
                    PermissionsTable.select { PermissionsTable.guildId.eq(guildId!!) }.toList()
                }

                val permsList = permsAssigned
                    .sortedByDescending { it[PermissionsTable.permission] }
                    .map {
                        val member = client.getMemberById(guildId!!, it[PermissionsTable.userId]).await()
                        val assignedBy = client.getMemberById(guildId!!, it[PermissionsTable.assignedById]).await()

                        "**${member.tag}**: ${it[PermissionsTable.permission]} (Given " +
                            "${it[PermissionsTable.assignedOn]} by ${assignedBy.tag})"
                    }.joinToString("\n")

                message.reply(permsList)
            }
        }

        PermissionType.values().filter { it != PermissionType.DEV }.map { permission ->
            literal(permission.name) {
                argument("users", greedyString()) {
                    require {
                        hasBotPermission(PermissionType.ADMIN) &&
                            member.basePermissions.await().contains(Permission.ADMINISTRATOR)
                    }

                    runs {
                        launch { getChannel().type().await() }

                        val membersUpdated = message.allMemberMentions
                            .map { it to it.botPermission() }
                            .filter { (memberToUpdate, _) -> memberToUpdate.updateBotPermission(permission, member) }
                            .toList()

                        message.reply(baseTemplate.andThen {
                            description = "Updated the following permissions:"
                            membersUpdated.forEach { (member, oldPerm) ->
                                field(member.tag, "$oldPerm->$permission", true)
                            }
                        })
                    }
                }
            }
        }
    }
}
