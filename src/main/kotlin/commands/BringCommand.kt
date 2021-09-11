package xyz.swagbot.commands

import com.mojang.brigadier.arguments.StringArgumentType.string
import discord4j.core.`object`.entity.channel.GuildChannel
import discord4j.core.`object`.entity.channel.VoiceChannel
import discord4j.rest.util.Permission
import discord4j.rest.util.PermissionSet
import io.facet.chatcommands.*
import io.facet.common.await
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import xyz.swagbot.extensions.hasBotPermission
import xyz.swagbot.features.permissions.PermissionType

object BringCommand : ChatCommand(
    name = "Bring Members",
    aliases = setOf("bring"),
    scope = Scope.GUILD,
    category = "moderator",
    discordPermsRequired = PermissionSet.of(Permission.MOVE_MEMBERS)
) {

    override fun DSLCommandNode<ChatCommandSource>.register() {
        require {
            hasBotPermission(PermissionType.MOD)
        }

        runs {

        }

        argument("channel", string()) {
            runs { context ->
                val guild = getGuild()

                val channelToBring: GuildChannel = guild.channels.await()
                    .firstOrNull { it is VoiceChannel && it.name == context.getString("channel") }
                    ?: return@runs

                launch {
                    guild.voiceStates.asFlow()
                        .filter { it.channelId.isPresent }
                        .map { vs -> vs.member.await().edit().withNewVoiceChannelOrNull(channelToBring.id) }
                        .buffer(100)
                        .collect {
                            it.await()
                        }
                }
            }
        }
    }
}
