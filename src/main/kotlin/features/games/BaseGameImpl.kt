package xyz.swagbot.features.games

import discord4j.core.`object`.entity.*
import discord4j.core.`object`.entity.channel.*
import io.facet.discord.extensions.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import xyz.swagbot.util.*

class BaseGameImpl(override val channel: GuildMessageChannel, scope: CoroutineScope) : Game, CoroutineScope by scope {

    override val players: Set<Member> = mutableSetOf()

    private val playerQueue: SendChannel<Member> = actor {
        players as MutableSet<Member>
        for (member in channel) {
            if (currentStage !is Game.Stage.PreGame) {
                this@BaseGameImpl.channel.createEmbed(errorTemplate.andThen {
                    it.setDescription("Sorry, the game has already started!")
                })
            } else {
                if (players.add(member)) {
                    this@BaseGameImpl.channel.createEmbed(baseTemplate.andThen {
                        it.setDescription(getJoinMessageForPlayer(member))
                    }).await()
                } else {
                    this@BaseGameImpl.channel.createEmbed(errorTemplate.andThen {
                        it.setDescription("You have already joined the game! It should be starting shortly.")
                    }).await()
                }
            }
        }
    }

    override var currentStage: Game.Stage = Game.Stage.PreGame

    override suspend fun join(player: Member) {
        playerQueue.send(player)
    }

    override fun getJoinMessageForPlayer(player: Member): String {
        return "**${player.displayName}** joined the game!"
    }
}
