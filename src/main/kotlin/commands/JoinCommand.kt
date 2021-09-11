package xyz.swagbot.commands

import io.facet.chatcommands.*
import io.facet.common.await
import io.facet.common.awaitNullable
import io.facet.common.reply
import xyz.swagbot.extensions.isMusicFeatureEnabled
import xyz.swagbot.extensions.joinWithAutoDisconnect
import xyz.swagbot.features.music.notPremiumTemplate

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
                channel.joinWithAutoDisconnect()
            } else {
                message.reply("You must be connected to a voice channel to summon me!")
            }
        }
    }
}