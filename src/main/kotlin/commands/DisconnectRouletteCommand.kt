package xyz.swagbot.commands

import discord4j.core.*
import io.facet.discord.commands.*
import io.facet.discord.commands.dsl.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactive.*
import xyz.swagbot.util.*

object DisconnectRouletteCommand : ChatCommand(
    name = "Disconnect Roulette",
    aliases = setOf("droulette"),
    scope = Scope.GUILD,
    category = "bgw"
) {

    override fun DSLCommandNode<ChatCommandSource>.register(client: GatewayDiscordClient) {
        runs {

            val connectedMembers = member!!
                .voiceState.await()
                .channel.awaitNullable()
                ?.voiceStates?.asFlow()
                ?.map { it.member.await() }
                ?.toList() ?: return@runs getChannel().createEmbed(errorTemplate.andThen { it.setDescription("") }).awaitComplete()

            connectedMembers[connectedMembers.indices.random()].edit {
                it.setNewVoiceChannel(null)
            }.await()
        }
    }
}
