package xyz.swagbot.features.music.commands

import com.mojang.brigadier.*
import io.facet.discord.commands.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*
import reactor.core.publisher.*
import xyz.swagbot.features.music.*
import java.time.*

object JoinCommand : ChatCommand {

    override fun register(dispatcher: CommandDispatcher<ChatCommandSource>) {
        dispatcher.register(literal("join").executes { context ->
            val source = context.source
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
                .subscribe()
                .let { 1 }
        })
    }
}