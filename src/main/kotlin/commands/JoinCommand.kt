package xyz.swagbot.commands

import io.facet.discord.commands.*
import io.facet.discord.commands.dsl.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*
import xyz.swagbot.extensions.*
import xyz.swagbot.features.music.*

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
                channel.join()
            } else {
                message.reply("You must be connected to a voice channel to summon me!")
            }
        }
    }
}