package xyz.swagbot.features.music.commands

import com.mojang.brigadier.*
import io.facet.discord.commands.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*
import xyz.swagbot.features.music.*

object PauseResumeCommand : ChatCommand {

    override fun register(dispatcher: CommandDispatcher<ChatCommandSource>) {
        dispatcher.register(literal("pause").executes { context ->
            val source = context.source
            source.event.guildId.ifPresent { guildId ->
                val feature = source.client.feature(Music).trackSchedulerFor(guildId)
            }.let { 1 }
        })
    }
}
