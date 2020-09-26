package xyz.swagbot.commands

import com.mojang.brigadier.arguments.StringArgumentType.*
import discord4j.core.`object`.entity.channel.*
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
                    ?: return@runs getChannel().createEmbed(errorTemplate.andThen {
                        it.setDescription("")
                    }).awaitComplete()

                launch {
                    guild.voiceStates.asFlow()
                        .filter { it.channelId.isPresent }
                        .map { vs -> vs.member.await().edit { it.setNewVoiceChannel(channelToBring.id) } }
                        .collect {
                            launch { it.await() }
                        }
                }
            }
        }
    }
}
