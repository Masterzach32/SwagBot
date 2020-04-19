package xyz.swagbot.features.permissions

import com.mojang.brigadier.*
import com.mojang.brigadier.arguments.StringArgumentType.*
import io.facet.discord.commands.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*
import reactor.core.publisher.*
import xyz.swagbot.util.*

object ChangePermissionCommand : ChatCommand {

    override fun register(dispatcher: CommandDispatcher<ChatCommandSource>) {
        val root = listOf("permission", "perm").map { literal(it) }

        root.forEach { alias ->
            PermissionType.values().filter { it != PermissionType.DEV }.map { permission ->
                alias.then(literal(permission.codeName).then(argument("users", greedyString()).requires {
                    it.hasBotPermission(PermissionType.ADMIN)
                }.executes { context ->
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
                        }.subscribe().let { 1 }
                }))
            }

            alias.then(argument("user", greedyString()).executes { context ->
                val source = context.source
                source.guildId.ifPresent { guildId ->
                    source.message.channel.flatMap { channel ->
                        source.message.userMentions.toMono().flatMap { user ->
                            source.client.getMemberById(guildId, user.id)
                        }.flatMap { member ->
                            val perm = source.client.feature(Permissions).permissionLevelFor(guildId, member.id)
                            channel.createEmbed(baseTemplate.andThen {
                                it.setDescription("**${member.displayName}** has permission **$perm**")
                            })
                        }
                    }.subscribe()
                }.let { 1 }
            }).executes { context ->
                val event = context.source
                event.guildId.ifPresent { guildId ->
                    val perm = event.client.feature(Permissions).permissionLevelFor(guildId, event.member.get().id)
                    event.message.channel.flatMap { channel ->
                        channel.createEmbed(baseTemplate.andThen {
                            it.setDescription("Your permission level is **$perm**")
                        })
                    }.subscribe()
                }.let { 1 }
            }

            dispatcher.register(alias)
        }
    }
}
