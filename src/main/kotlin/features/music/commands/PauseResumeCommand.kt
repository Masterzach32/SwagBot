package xyz.swagbot.features.music.commands

import discord4j.core.*
import io.facet.discord.commands.*
import io.facet.discord.commands.dsl.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*
import xyz.swagbot.extensions.*
import xyz.swagbot.util.*

object PauseResumeCommand : ChatCommand(
    name = "Pause/Resume",
    aliases = setOf("pause", "resume"),
    scope = Scope.GUILD,
    category = "music"
) {

    override fun DSLCommandNode<ChatCommandSource>.register(client: GatewayDiscordClient) {
        runs { context ->
            val guild = getGuild()
            val channel = getChannel()

            if (!isMusicFeatureEnabled())
                return@runs channel.createEmbed(notPremiumTemplate(prefixUsed)).awaitComplete()

            TODO()
        }
    }
}
