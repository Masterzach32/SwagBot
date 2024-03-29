package xyz.swagbot.features.games

import discord4j.core.`object`.entity.Member
import io.facet.common.await
import io.facet.common.awaitComplete
import io.facet.common.dsl.and
import io.facet.common.sendMessage
import kotlinx.coroutines.channels.ticker
import xyz.swagbot.logger
import xyz.swagbot.util.errorTemplate

class Brawl(game: Game) : Game by game {

    private val ticker = ticker(1000, 20_000)

    override suspend fun run() {
        logger.info("Fight starting in 20 seconds.")
        ticker.receive()
        if (players.size <= 1) {
            channel.sendMessage(errorTemplate.and {
                description = "The brawl failed to start because not enough players joined!"
            })
            return
        }
        channel.createMessage("Let the brawl begin!").awaitComplete()

        ticker.receive()
        currentStage = Game.Stage.InGame
        val players = players.toMutableList()
        while (players.size > 1) {
            var str = ""
            for (j in players.indices)
                if (j < players.size - 1)
                    str += "**${players[j].displayName}**, "
                else
                    str += "and **${players[j].displayName}** are fighting!"
            val msg = channel.createMessage(str).await()
            ticker.receive()

            var dead: Member
            do {
                dead = players.random()
            } while (dead.id.asLong() == 0L)
            players.remove(dead)

            val killer = players.random()
        }
    }
}