package xyz.swagbot.features.music.commands

import io.facet.discord.commands.*
import io.facet.discord.commands.dsl.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*
import xyz.swagbot.extensions.*
import xyz.swagbot.features.music.*
import xyz.swagbot.util.*

object JoinCommand : ChatCommand(
    name = "Join Voice",
    aliases = setOf("join", "summon"),
    scope = Scope.GUILD,
    category = "music"
) {

    override fun DSLCommandNode<ChatCommandSource>.register() {
        runs { context ->
            if (!isMusicFeatureEnabled()) {
                respondEmbed(notPremiumTemplate(prefixUsed))
                return@runs
            }

            val voiceChannel = member
                .voiceState.await()
                .channel.awaitNullable()

            if (voiceChannel != null) {
                voiceChannel.join()
            } else {
                respondEmbed(errorTemplate.andThen {
                    description = "You must be connected to a voice channel to summon me!"
                })
            }
        }
    }
}