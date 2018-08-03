package xyz.swagbot.api.music

import sx.blah.discord.handle.obj.IUser

class TrackUserData(val requester: IUser) {

    private val skipList = mutableSetOf<IUser>()

    fun addSkipVote(user: IUser) = skipList.add(user)

    fun getSkipVoteCount() = skipList.size
}