package xyz.swagbot.features.games

import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.channel.GuildMessageChannel
import io.facet.common.dsl.and
import io.facet.common.sendMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import xyz.swagbot.util.baseTemplate
import xyz.swagbot.util.errorTemplate

class BaseGameImpl(override val channel: GuildMessageChannel, scope: CoroutineScope) : Game, CoroutineScope by scope {

    override val players: Set<Member> = mutableSetOf()

    private val playerQueue: SendChannel<Member> = actor {
        players as MutableSet<Member>
        for (member in channel) {
            if (currentStage !is Game.Stage.PreGame) {
                this@BaseGameImpl.channel.sendMessage(errorTemplate.and {
                    description = "Sorry, the game has already started!"
                })
            } else {
                if (players.add(member)) {
                    this@BaseGameImpl.channel.sendMessage(baseTemplate.and {
                        description = getJoinMessageForPlayer(member)
                    })
                } else {
                    this@BaseGameImpl.channel.sendMessage(errorTemplate.and {
                        description = "You have already joined the game! It should be starting shortly."
                    })
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
