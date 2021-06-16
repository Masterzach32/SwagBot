package xyz.swagbot.extensions

import com.sedmelluq.discord.lavaplayer.track.*
import discord4j.common.util.*
import discord4j.core.`object`.entity.*
import discord4j.core.`object`.entity.channel.*
import xyz.swagbot.features.music.*

val AudioTrack.context: TrackContext
    get() = getUserData(TrackContext::class.java)

fun AudioTrack.setTrackContext(member: Member, channel: MessageChannel) = setTrackContext(member.id, channel.id)

fun AudioTrack.setTrackContext(memberId: Snowflake, channelId: Snowflake) {
    userData = TrackContext(memberId, channelId)
}

val AudioTrack.formattedPosition: String
    get() = getFormattedTime(position/1000)

val AudioTrack.formattedLength: String
    get() = getFormattedTime(duration/1000)
