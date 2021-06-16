package xyz.swagbot.commands

import io.facet.discord.commands.*
import io.facet.discord.commands.dsl.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactive.*

object DisconnectRouletteCommand : ChatCommand(
    name = "Disconnect Roulette",
    aliases = setOf("droulette"),
    scope = Scope.GUILD,
    category = "bgw"
) {

    override fun DSLCommandNode<ChatCommandSource>.register() {
        runs {
            val connectedMembers = member.getConnectedVoiceChannel()?.getConnectedMembers() ?: return@runs

            connectedMembers.random().let { member ->
                member.edit {
                    it.setNewVoiceChannel(null)
                }.await()
                message.reply("The roulette has chosen **${member.displayName}**!")
            }
        }
    }
}
