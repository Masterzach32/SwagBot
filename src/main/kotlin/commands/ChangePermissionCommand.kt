package xyz.swagbot.commands

import com.mojang.brigadier.arguments.StringArgumentType.greedyString
import com.mojang.brigadier.arguments.StringArgumentType.string
import discord4j.rest.util.ApplicationCommandOptionType
import discord4j.rest.util.Permission
import io.facet.chatcommands.ChatCommandSource
import io.facet.chatcommands.DSLCommandNode
import io.facet.chatcommands.runs
import io.facet.commands.GuildApplicationCommand
import io.facet.commands.GuildSlashCommandContext
import io.facet.commands.applicationCommandRequest
import io.facet.common.allMemberMentions
import io.facet.common.await
import io.facet.common.dsl.and
import io.facet.common.reply
import io.facet.common.toSnowflake
import io.facet.exposed.sql
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.select
import xyz.swagbot.extensions.botPermission
import xyz.swagbot.extensions.hasBotPermission
import xyz.swagbot.extensions.updateBotPermission
import xyz.swagbot.features.permissions.PermissionType
import xyz.swagbot.features.permissions.PermissionsTable
import xyz.swagbot.util.baseTemplate

object ChangePermissionCommand : GuildApplicationCommand/*(
    name = "Change User Permissions",
    aliases = setOf("permission", "perm"),
    scope = Scope.GUILD,
    category = "admin",
    usage = commandUsage {
        default("View your current permission level.")
        add("<member>", "View another member's permission level.")
        add("<permission> <members/roles..>", "Add the specified permission to the mentioned members.")
    }
)*/ {

    override val guildId = 97342233241464832.toSnowflake()

    override val request = applicationCommandRequest("permissions", "Get or edit bot permissions for a server member") {
        subCommand("get", "Get bot permissions for a server member") {
            option("user", "The server member to get the bot permission for", ApplicationCommandOptionType.USER, false)
        }
        subCommand("set", "Edit the bot permissions for a server member") {
            option("user", "The server member to edit", ApplicationCommandOptionType.USER, true)
            option("permission", "The bot permission to set", ApplicationCommandOptionType.STRING, true) {
                for (perm in PermissionType.allAvailable())
                    choice(perm.codeName, "")
            }
        }
    }

    override suspend fun GuildSlashCommandContext.execute() {
        when {
            event.getOption("get").isPresent -> get()
            event.getOption("set").isPresent -> set()
        }
    }

    private suspend fun GuildSlashCommandContext.get() {

    }

    private suspend fun GuildSlashCommandContext.set() {

    }

    fun DSLCommandNode<ChatCommandSource>.register() {
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

                        message.reply(baseTemplate.and {
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
