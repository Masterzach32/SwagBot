package xyz.swagbot.features.games

import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.channel.GuildMessageChannel
import kotlinx.coroutines.CoroutineScope

interface Game : CoroutineScope {

    val channel: GuildMessageChannel

    val players: Set<Member>

    var currentStage: Stage

    suspend fun run() {
        throw NotImplementedError("Did you forget to override the run() function of the game?")
    }

    suspend fun join(player: Member)

    fun getJoinMessageForPlayer(player: Member): String

    sealed class Stage {
        object PreGame : Stage()
        object InGame : Stage()
        object Finished : Stage()
    }
}
