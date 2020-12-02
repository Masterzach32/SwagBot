package xyz.swagbot.features.music.commands

import io.facet.discord.commands.*
import io.facet.discord.commands.dsl.*
import io.facet.discord.commands.extensions.*
import xyz.swagbot.extensions.*
import xyz.swagbot.features.permissions.*

object Clear : ChatCommand(
    name = "Clear Queue",
    aliases = setOf("clear"),
    scope = Scope.GUILD,
    category = "music",
    description = "Clear the music queue."
) {

    override fun DSLCommandNode<ChatCommandSource>.register() {
        require { hasBotPermission(PermissionType.MOD) }

        runs {
            getGuild().trackScheduler.apply {
                clearQueue()
                playNext()
            }
        }
    }
}
