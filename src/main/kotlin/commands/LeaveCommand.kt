package xyz.swagbot.commands

import io.facet.chatcommands.*
import io.facet.common.await
import io.facet.common.reply
import xyz.swagbot.extensions.isMusicFeatureEnabled
import xyz.swagbot.extensions.setLastConnectedChannel
import xyz.swagbot.features.music.notPremiumTemplate

object LeaveCommand : ChatCommand(
    name = "Leave Voice",
    aliases = setOf("leave"),
    scope = Scope.GUILD,
    category = "music"
) {

    override fun DSLCommandNode<ChatCommandSource>.register() {
        runs { context ->
            val guild = getGuild()
            if (!isMusicFeatureEnabled()) {
                message.reply(notPremiumTemplate(prefixUsed))
                return@runs
            }

            guild.voiceConnection.await().disconnect().await()

            guild.setLastConnectedChannel(null)
        }
    }
}
