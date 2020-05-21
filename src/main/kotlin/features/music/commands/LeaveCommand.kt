package xyz.swagbot.features.music.commands

import com.mojang.brigadier.builder.*
import discord4j.core.*
import io.facet.discord.commands.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*
import reactor.core.publisher.*
import xyz.swagbot.extensions.*
import xyz.swagbot.features.music.*

object LeaveCommand : ChatCommand(
    name = "Leave Voice",
    aliases = setOf("leave"),
    scope = Scope.GUILD,
    category = "music"
) {

    override fun register(client: DiscordClient, node: LiteralArgumentBuilder<ChatCommandSource>) {
        node.executesAsync { context ->
            val source = context.source
            source.handlePremium().switchIfEmpty {
                source.event.guild.flatMap { guild ->
                    source.client.feature(Music).let { feature ->
                        feature.voiceConnections.remove(guild.id)?.disconnect()
                        feature.updateCurrentlyConnectedChannelFor(guild.id, null)
                    }
                }
            }
        }
    }
}
