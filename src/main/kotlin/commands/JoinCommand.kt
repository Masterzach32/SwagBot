package xyz.swagbot.commands

import discord4j.core.event.domain.*
import io.facet.core.extensions.*
import io.facet.discord.commands.*
import io.facet.discord.commands.dsl.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactive.*
import xyz.swagbot.extensions.*
import xyz.swagbot.features.music.*
import xyz.swagbot.util.*

object JoinCommand : ChatCommand(
    name = "Join Voice",
    aliases = setOf("join", "summon"),
    scope = Scope.GUILD,
    category = "music"
) {

    override fun DSLCommandNode<ChatCommandSource>.register() {
        runs { context ->
            if (!isMusicFeatureEnabled()) {
                message.reply(notPremiumTemplate(prefixUsed))
                return@runs
            }

            val channel = member
                    .voiceState.await()
                    .channel.awaitNullable()

            if (channel != null) {
                channel.join(this)
            } else {
                message.reply("You must be connected to a voice channel to summon me!")
            }
        }
    }
}