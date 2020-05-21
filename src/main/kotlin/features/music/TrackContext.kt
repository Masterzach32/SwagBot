package xyz.swagbot.features.music

import discord4j.core.`object`.util.*

class TrackContext(val requesterId: Snowflake, val requestedChannelId: Snowflake) {

    private val skipList = mutableSetOf<Snowflake>()

    val skipVoteCount: Int
        get() = skipList.size

    fun addSkipVote(userId: Snowflake): Boolean = skipList.add(userId)
}
