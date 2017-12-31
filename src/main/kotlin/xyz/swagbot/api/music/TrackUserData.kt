package xyz.swagbot.api.music

import sx.blah.discord.handle.obj.IUser

class TrackUserData(val author: IUser) {

    private val skipList = mutableSetOf<IUser>()

    fun addSkipVote(user: IUser): Boolean {
        return skipList.add(user)
    }

    fun getSkipVoteCount(): Int {
        return skipList.size
    }
}