package xyz.swagbot.features.music.commands

import com.mojang.brigadier.builder.*
import discord4j.core.*
import io.facet.discord.commands.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*
import reactor.core.publisher.*
import xyz.swagbot.extensions.*
import xyz.swagbot.features.music.*
import java.time.*

object JoinCommand : ChatCommand(
    name = "Join Voice",
    aliases = setOf("join", "summon"),
    scope = Scope.GUILD,
    category = "music"
) {

    override fun register(client: DiscordClient, node: LiteralArgumentBuilder<ChatCommandSource>) {
        node.executesAsync { context ->
            val source = context.source
            source.handlePremium()
                .switchIfEmpty {
                    Mono.justOrEmpty(source.member)
                        .flatMap { it.voiceState }
                        .flatMap { it.channel }
                        .flatMap { vc ->
                            vc.join {
                                it.setProvider(source.client.feature(Music).trackSchedulerFor(vc.guildId).audioProvider)
                            }
                        }
                        .timeout(Duration.ofSeconds(10L))
                        .map { source.client.feature(Music).voiceConnections[source.event.guildId.get()] = it }
                        .then()
                }
        }
    }
}