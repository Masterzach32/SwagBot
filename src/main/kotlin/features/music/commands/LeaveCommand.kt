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
            source.event.guildId.ifPresent { guildId ->
                source.client.feature(Music).apply {
                    voiceConnections.remove(guildId)?.disconnect()
                    updateCurrentlyConnectedChannelFor(guildId, null)
                }
            }.let { 1 }
        })
    }
}
