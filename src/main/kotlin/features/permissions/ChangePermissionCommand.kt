package xyz.swagbot.features.permissions

import com.mojang.brigadier.arguments.StringArgumentType.*
import com.mojang.brigadier.builder.*
import discord4j.core.*
import io.facet.discord.commands.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*
import reactor.core.publisher.*
import xyz.swagbot.extensions.*
import xyz.swagbot.util.*

object ChangePermissionCommand : ChatCommand(
    name = "Change User Permissions",
    aliases = setOf("permission", "perm"),
    scope = Scope.GUILD
) {

    override fun register(client: DiscordClient, node: LiteralArgumentBuilder<ChatCommandSource>) {

        PermissionType.values().filter { it != PermissionType.DEV }.map { permission ->
            node.then(literal(permission.codeName).then(argument("users", greedyString()).requires {
                it.hasBotPermission(PermissionType.ADMIN)
            }.executesAsync { context ->
                val feature = context.source.client.feature(Permissions)

                context.source.guild.flatMap { guild ->
                    context.source.message.allUserMentions
                        .map { member ->
                            feature.updatePermissionFor(guild.id, member.id, permission, context.source.member.get().id)
                        }
                        .filter { it }
                        .collectList()
                        .map { it.size }
                        .map { usersUpdated ->
                            context.source.message.channel.flatMap { channel ->
                                channel.createEmbed(baseTemplate.andThen {
                                    it.setDescription("Updated permissions for **$usersUpdated** members.")
                                })
                            }
                        }
                        .then()
                    }
            }))
        }

        node.then(argument("user", greedyString()).executesAsync { context ->
            val source = context.source
            source.message.channel.flatMap { channel ->
                source.message.userMentions.toMono().flatMap { user ->
                    source.client.getMemberById(source.guildId.get(), user.id)
                }.flatMap { member ->
                    val perm = source.client.feature(Permissions).permissionLevelFor(source.guildId.get(), member.id)
                    channel.createEmbed(baseTemplate.andThen {
                        it.setDescription("**${member.displayName}** has permission **$perm**")
                    })
                }.then()
            }
        }).executesAsync { context ->
            val source = context.source
            val perm = source.client.feature(Permissions).permissionLevelFor(source.guildId.get(), source.member.get().id)
            source.message.channel.flatMap { channel ->
                channel.createEmbed(baseTemplate.andThen {
                    it.setDescription("Your permission level is **$perm**")
                })
            }.then()
        }
    }
}
