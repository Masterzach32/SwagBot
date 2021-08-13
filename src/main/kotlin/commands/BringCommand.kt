package xyz.swagbot.commands

import com.mojang.brigadier.arguments.StringArgumentType.*
import discord4j.core.`object`.entity.channel.*
import discord4j.rest.util.*
import io.facet.chatcommands.*
import io.facet.common.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactive.*
import xyz.swagbot.extensions.*
import xyz.swagbot.features.permissions.*

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
