package xyz.swagbot.commands

import io.facet.chatcommands.*
import xyz.swagbot.extensions.hasBotPermission
import xyz.swagbot.extensions.trackScheduler
import xyz.swagbot.features.permissions.PermissionType

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
