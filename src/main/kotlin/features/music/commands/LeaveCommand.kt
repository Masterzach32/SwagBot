package xyz.swagbot.features.music.commands

import io.facet.discord.commands.*
import io.facet.discord.commands.dsl.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*
import xyz.swagbot.extensions.*
import xyz.swagbot.util.*

object LeaveCommand : ChatCommand(
    name = "Leave Voice",
    aliases = setOf("leave"),
    scope = Scope.GUILD,
    category = "music"
) {

    override fun DSLCommandNode<ChatCommandSource>.register() {
        runs { context ->
            val channel = getChannel()
            val guild = getGuild()
            if (!isMusicFeatureEnabled())
                return@runs channel.createEmbed(notPremiumTemplate(prefixUsed)).awaitComplete()

            client.voiceConnectionRegistry.disconnect(guildId!!).await()

            guild.setLastConnectedChannel(null)
        }
    }
}
