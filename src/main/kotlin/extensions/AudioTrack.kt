package xyz.swagbot.extensions

import com.sedmelluq.discord.lavaplayer.track.*
import discord4j.core.`object`.entity.*
import xyz.swagbot.features.music.*

val AudioTrack.trackContext: TrackContext
    get() = getUserData(TrackContext::class.java)

fun AudioTrack.setTrackContext(member: Member, channel: MessageChannel) {
    userData = TrackContext(member.id, channel.id)
}

val AudioTrack.formattedPosition: String
    get() = getFormattedTime(position/1000)

val AudioTrack.formattedLength: String
    get() = getFormattedTime(duration/1000)
