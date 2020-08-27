package xyz.swagbot.features.music

import discord4j.common.util.*

class TrackContext(val requesterId: Snowflake, val requestedChannelId: Snowflake) {

    private val skipList: MutableSet<Snowflake> = mutableSetOf()

    val skipVoteCount: Int
        get() = skipList.size

    fun addSkipVote(userId: Snowflake): Boolean = skipList.add(userId)
}
