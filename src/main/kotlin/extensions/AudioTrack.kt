package xyz.swagbot.extensions

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.channel.MessageChannel
import xyz.swagbot.features.music.TrackContext
import xyz.swagbot.features.music.getFormattedTime

val AudioTrack.context: TrackContext
    get() = getUserData(TrackContext::class.java)

fun AudioTrack.setTrackContext(member: Member, channel: MessageChannel) = setTrackContext(member.id, channel.id)

fun AudioTrack.setTrackContext(memberId: Snowflake, channelId: Snowflake) {
    userData = TrackContext(memberId, channelId)
}

val AudioTrack.formattedPosition: String
    get() = getFormattedTime(position / 1000)

val AudioTrack.formattedLength: String
    get() = getFormattedTime(duration / 1000)
