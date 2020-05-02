package xyz.swagbot.features.music.commands

import com.mojang.brigadier.*
import io.facet.discord.commands.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*
import xyz.swagbot.features.music.*

object LeaveCommand : ChatCommand {

    override fun register(dispatcher: CommandDispatcher<ChatCommandSource>) {
        dispatcher.register(literal("leave").executes { context ->
            val source = context.source
            source.event.guild.flatMap { guild ->
                source.client.feature(Music).let { feature ->
                    feature.voiceConnections.remove(guild.id)?.disconnect()
                    feature.updateCurrentlyConnectedChannelFor(guild.id, null)
                }
            }.subscribe().let { 1 }
        })
    }
}
