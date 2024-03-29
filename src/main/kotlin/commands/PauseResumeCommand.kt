package xyz.swagbot.commands

import io.facet.chatcommands.*
import io.facet.common.reply
import xyz.swagbot.extensions.isMusicFeatureEnabled
import xyz.swagbot.features.music.notPremiumTemplate

object PauseResumeCommand : ChatCommand(
    name = "Pause/Resume",
    aliases = setOf("pause", "resume"),
    scope = Scope.GUILD,
    category = "music"
) {

    override fun DSLCommandNode<ChatCommandSource>.register() {
        runs { context ->

            if (!isMusicFeatureEnabled()) {
                message.reply(notPremiumTemplate(prefixUsed))
                return@runs
            }

            TODO()
        }
    }
}
