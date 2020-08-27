package xyz.swagbot.features.music.commands

import discord4j.core.*
import io.facet.discord.commands.*
import io.facet.discord.commands.dsl.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*
import xyz.swagbot.extensions.*
import xyz.swagbot.util.*

object JoinCommand : ChatCommand(
    name = "Join Voice",
    aliases = setOf("join", "summon"),
    scope = Scope.GUILD,
    category = "music"
) {

    override fun DSLCommandNode<ChatCommandSource>.register(client: GatewayDiscordClient) {
        runs { context ->
            val channel = getChannel()

            if (!isMusicFeatureEnabled())
                return@runs channel.createEmbed(notPremiumTemplate(prefixUsed)).awaitComplete()

            val voiceChannel = member!!
                .voiceState.await()
                .channel.awaitNullable()

            if (voiceChannel != null) {
                voiceChannel.joinVoice()
            } else {
                channel.createEmbed(errorTemplate.andThen {
                    it.setDescription("You must be connected to a voice channel to summon me!")
                }).await()
            }
        }
    }
}