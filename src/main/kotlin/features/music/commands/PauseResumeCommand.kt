package xyz.swagbot.features.music.commands

import io.facet.discord.commands.*
import io.facet.discord.commands.dsl.*
import io.facet.discord.commands.extensions.*
import xyz.swagbot.extensions.*
import xyz.swagbot.features.music.*

object PauseResumeCommand : ChatCommand(
    name = "Pause/Resume",
    aliases = setOf("pause", "resume"),
    scope = Scope.GUILD,
    category = "music"
) {

    override fun DSLCommandNode<ChatCommandSource>.register() {
        runs { context ->

            if (!isMusicFeatureEnabled()) {
                respondEmbed(notPremiumTemplate(prefixUsed))
                return@runs
            }

            TODO()
        }
    }
}
