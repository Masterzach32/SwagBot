package xyz.swagbot.features.music.commands

import com.mojang.brigadier.arguments.IntegerArgumentType.*
import com.mojang.brigadier.builder.*
import discord4j.core.*
import io.facet.discord.commands.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*
import reactor.core.publisher.*
import xyz.swagbot.extensions.*
import xyz.swagbot.features.music.*
import xyz.swagbot.util.*

object VolumeCommand : ChatCommand(
    name = "Volume",
    aliases = setOf("volume", "v"),
    scope = Scope.GUILD,
    category = "music"
) {

    override fun register(client: DiscordClient, node: LiteralArgumentBuilder<ChatCommandSource>) {
        node.then(argument("level", integer(0, 100)).executesAsync { context ->
            val source = context.source
            source.handlePremium().switchIfEmpty {
                val newVol = context.getInt("level")
                source.channel.flatMap { channel ->
                    channel.createEmbed(baseTemplate.andThen {
                        it.setDescription("Volume changed to **$newVol**")
                    })
                }.flatMap { source.client.feature(Music).updateVolumeFor(source.guildId.get(), newVol) }
            }
        }).executesAsync { context ->
            val source = context.source
            source.client.feature(Music).volumeFor(source.guildId.get()).flatMap { vol ->
                source.message.channel.flatMap { channel ->
                    channel.createEmbed(baseTemplate.andThen {
                        it.setDescription("Volume is at **$vol**")
                    })
                }
            }.then()
        }
    }
}
