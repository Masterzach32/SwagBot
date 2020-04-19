package xyz.swagbot.features.music

import discord4j.core.`object`.util.*

class TrackContext(val requester: Snowflake, val requestedChannel: Snowflake) {

    private val skipList = mutableSetOf<Snowflake>()

    val skipVoteCount: Int
        get() = skipList.size

    fun addSkipVote(userId: Snowflake) = skipList.add(userId)
}
