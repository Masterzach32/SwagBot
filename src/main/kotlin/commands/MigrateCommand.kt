package xyz.swagbot.commands

import com.mojang.brigadier.arguments.StringArgumentType.*
import discord4j.rest.util.*
import io.facet.discord.commands.*
import io.facet.discord.commands.dsl.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactive.*
import xyz.swagbot.extensions.*
import xyz.swagbot.features.permissions.*
import xyz.swagbot.util.*

object MigrateCommand : ChatCommand(
    name = "Migrate Members",
    aliases = setOf("migrate", "m"),
    scope = Scope.GUILD,
    category = "moderator",
    discordPermsRequired = PermissionSet.of(Permission.MOVE_MEMBERS)
) {

    override fun DSLCommandNode<ChatCommandSource>.register() {
        require {
            hasBotPermission(PermissionType.MOD)
        }

        argument("fromChannel", string()) {
            argument("toChannel", string()) {
                runs { context ->
                    val guild = getGuild()
                    val fromChannelName = context.getString("fromChannel")
                    val fromChannel = guild.getVoiceChannelByName(fromChannelName)
                    val toChannelName = context.getString("toChannel")
                    val toChannel = guild.getVoiceChannelByName(toChannelName)

                    if (fromChannel == null) {
                        respondEmbed(errorTemplate.andThen {
                            description = "I could not find a voice channel with the name **${fromChannelName}**"
                        })
                        return@runs
                    }
                    if (toChannel == null) {
                        respondEmbed(errorTemplate.andThen {
                            description = "I could not find a voice channel with the name **${toChannelName}**"
                        })
                        return@runs
                    }

                    val channel = getChannel()
                    launch { channel.type().await() }

                    val numMoved = fromChannel.voiceStates.asFlow()
                        .map { it.member.await() }
                        .toList()
                        .map { member -> member.edit { it.setNewVoiceChannel(toChannel.id) } }
                        .map { launch { it.await() } }
                        .count()

                    channel.sendEmbed(baseTemplate.andThen {
                        description = "Moved **${numMoved}** members from **${fromChannelName}** to **${toChannelName}**"
                    })
                }
            }
        }
    }
}
