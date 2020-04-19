package xyz.swagbot.features.music.commands

import com.mojang.brigadier.*
import com.mojang.brigadier.arguments.IntegerArgumentType.*
import io.facet.discord.commands.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*
import xyz.swagbot.features.music.*
import xyz.swagbot.util.*

object VolumeCommand : ChatCommand {

    override fun register(dispatcher: CommandDispatcher<ChatCommandSource>) {
        listOf("volume", "v").forEach { alias ->
            dispatcher.register(literal(alias).then(argument("level", integer(0, 100)).executes { context ->
                val source = context.source
                source.event.guildId.ifPresent { guildId ->
                    val newVol = context.getInt("level")
                    source.client.feature(Music).updateVolumeFor(guildId, newVol)
                    source.message.channel
                        .flatMap { channel ->
                            channel.createEmbed {
                                it.setColor(BLUE)
                                it.setDescription("Volume changed to **$newVol**")
                            }
                        }
                        .subscribe()
                }.let { 1 }

            }).executes { context ->
                val source = context.source
                source.event.guildId.ifPresent { guildId ->
                    val vol = source.client.feature(Music).volumeFor(guildId)
                    source.message.channel
                        .flatMap { channel ->
                            channel.createEmbed {
                                it.setColor(BLUE)
                                it.setDescription("Volume is at **$vol**")
                            }
                        }
                        .subscribe()
                }.let { 1 }
            })
        }
    }
}
