package xyz.swagbot.features.music.commands

import com.mojang.brigadier.builder.*
import discord4j.core.*
import io.facet.discord.commands.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*
import reactor.core.publisher.*
import xyz.swagbot.extensions.*
import xyz.swagbot.features.music.*
import xyz.swagbot.features.permissions.*
import xyz.swagbot.util.*

object SkipCommand : ChatCommand(
    name = "Skip Track",
    aliases = setOf("skip"),
    scope = Scope.GUILD,
    category = "music"
) {

    override fun register(client: DiscordClient, node: LiteralArgumentBuilder<ChatCommandSource>) {
        node.executesAsync { context ->
            val source = context.source
            source.handlePremium().switchIfEmpty(
                source.handleBotPerm(
                    PermissionType.MOD,
                    "You don't have permission to instantly skip a track. Try using " +
                            "`${source.commandPrefixUsed}voteskip` instead."
                )
            ).switchIfEmpty(
                source.channel.flatMap { channel ->
                    source.guild.flatMap { guild ->
                        guild.ourConnectedVoiceChannel.flatMap { vc ->
                            source.member.get().voiceState
                                .filterWhen { memberVs ->
                                    memberVs.channel.map { it.id == vc.id }
                                }
                                .flatMap { _ ->
                                    source.client.feature(Music).trackSchedulerFor(source.guildId.get()).playNext()
                                        .toMonoOrEmpty()
                                        .flatMap { track ->
                                            channel.createEmbed(baseTemplate.andThen {
                                                it.setDescription("Skipped track: ${track.info.boldFormattedTitle}")
                                            })
                                        }
                                        .switchIfEmpty {
                                            channel.createEmbed(errorTemplate.andThen {
                                                it.setDescription("Cannot skip as there is no track playing!")
                                            })
                                        }
                                        .then()
                                }
                                .switchIfEmpty {
                                    channel.createEmbed(errorTemplate.andThen {
                                        it.setDescription("You must be in ${vc.name} to skip a track!")
                                    }).then()
                                }
                        }.switchIfEmpty {
                            channel.createEmbed(errorTemplate.andThen {
                                it.setDescription("The bot is currently not in a voice channel!")
                            }).then()
                        }
                    }
                }
            )
        }
    }
}
