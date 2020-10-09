package xyz.swagbot.features.music.commands

import io.facet.discord.commands.*
import io.facet.discord.commands.dsl.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*
import xyz.swagbot.extensions.*
import xyz.swagbot.features.music.*

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
                respondEmbed(notPremiumTemplate(prefixUsed))
                return@runs
            }

            client.voiceConnectionRegistry.disconnect(guildId!!).await()

            guild.setLastConnectedChannel(null)
        }
    }
}
