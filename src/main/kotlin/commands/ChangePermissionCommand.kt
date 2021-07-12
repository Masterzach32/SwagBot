package xyz.swagbot.commands

import com.mojang.brigadier.arguments.StringArgumentType.*
import discord4j.rest.util.*
import io.facet.discord.appcommands.*
import io.facet.discord.commands.*
import io.facet.discord.commands.dsl.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.dsl.*
import io.facet.discord.exposed.*
import io.facet.discord.extensions.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.jetbrains.exposed.sql.*
import xyz.swagbot.extensions.*
import xyz.swagbot.features.permissions.*
import xyz.swagbot.util.*

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
        addSubCommand("get", "Get bot permissions for a server member") {
            addOption("user", "The server member to get the bot permission for", ApplicationCommandOptionType.USER, false)
        }
        addSubCommand("set", "Edit the bot permissions for a server member") {
            addOption("user", "The server member to edit", ApplicationCommandOptionType.USER, true)
            addOption("permission", "The bot permission to set", ApplicationCommandOptionType.STRING, true) {
                for (perm in PermissionType.allAvailable())
                    addChoice(perm.codeName, "")
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
