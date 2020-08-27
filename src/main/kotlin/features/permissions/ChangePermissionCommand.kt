package xyz.swagbot.features.permissions

import com.mojang.brigadier.arguments.StringArgumentType.*
import discord4j.core.*
import discord4j.core.`object`.entity.*
import discord4j.rest.util.*
import io.facet.discord.commands.*
import io.facet.discord.commands.dsl.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*
import kotlinx.coroutines.reactive.*
import xyz.swagbot.extensions.*
import xyz.swagbot.util.*

object ChangePermissionCommand : ChatCommand(
    name = "Change User Permissions",
    aliases = setOf("permission", "perm"),
    scope = Scope.GUILD,
    category = "admin",
    discordPermsRequired = PermissionSet.of(Permission.ADMINISTRATOR)
) {

    override fun DSLCommandNode<ChatCommandSource>.register(client: GatewayDiscordClient) {
        runs {
            val perm = member!!.botPermission()
            getChannel().createEmbed(baseTemplate.andThen {
                it.setDescription("Your permission level is **$perm**")
            }).awaitComplete()
        }

        argument("user", greedyString()) {
            runs {
                val user = message.userMentions.awaitFirst()
                val member = user as? Member ?: client.getMemberById(guildId!!, user.id).await()

                val perm = member.botPermission()
                getChannel().createEmbed(baseTemplate.andThen {
                    it.setDescription("**${member.displayName}** has permission **$perm**")
                }).awaitComplete()
            }
        }

        PermissionType.values().filter { it != PermissionType.DEV }.map { permission ->
            literal(permission.codeName) {
                argument("users", greedyString()) {
                    require { hasBotPermission(PermissionType.ADMIN) }

                    runs {
                        val channel = getChannel().also {
                            it.type().async()
                        }

                        val guild = getGuild()
                        val assignedBy = member!!

                        val allUserMentions = message.getAllUserMentions()

                        val membersUpdated = allUserMentions
                            .filter { it.updateBotPermission(permission, assignedBy, guild.id) }

                        channel.createEmbed(baseTemplate.andThen {
                            it.setDescription("Updated permissions for **${membersUpdated.size}** members.")
                        }).awaitComplete()
                    }
                }
            }
        }
    }
}
