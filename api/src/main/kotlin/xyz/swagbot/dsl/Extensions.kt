package xyz.swagbot.dsl

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo
import sx.blah.discord.handle.obj.*
import sx.blah.discord.util.RequestBuffer
import xyz.swagbot.api.music.TrackUserData
import xyz.swagbot.database.getTrackPreferences
import xyz.swagbot.utils.getFormattedTime

/*
 * SwagBot - Created on 8/30/2017
 * Author: Zach Kozar
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at the project root.
 */

/**
 * @author Zach Kozar
 * @version 8/30/2017
 */
fun IMessage.getAllUserMentions(): List<IUser> {
    if (mentionsEveryone())
        return guild.users
    val users = mutableSetOf<IUser>()
    if (mentionsHere())
        users.addAll(guild.users.filter { it.presence.status != StatusType.OFFLINE })
    roleMentions.forEach { users.addAll(guild.getUsersByRole(it)) }
    users.addAll(mentions)
    return users.toList()
}

fun AudioTrack.getFormattedPosition(): String {
    return getFormattedTime(position.toInt()/1000)
}

fun AudioTrack.getFormattedLength(): String {
    return getFormattedTime(duration.toInt()/1000)
}

fun AudioTrack.getTrackUserData(): TrackUserData {
    return getUserData(TrackUserData::class.java)
}

fun AudioTrack.getBoldFormattedTitle(): String {
    return "**${info.title}** by **${info.author}**"
}

fun AudioTrack.getFormattedTitle(): String {
    return "${info.title} by ${info.author}"
}

fun AudioTrack.getFormattedTitleWithTime(): String {
    return "${info.title} by ${info.author} - **${getFormattedLength()}**"
}

fun AudioTrack.getFormattedTitleAsLink(): String {
    return "**[${info.title}](${info.uri})**"
}

fun AudioTrack.getRequester(): IUser = getTrackUserData().requester

fun AudioTrackInfo.hasThumbnail(): Boolean = uri.contains("youtu")

fun AudioTrackInfo.getThumbnailUrl(): String = "https://img.youtube.com/vi/$identifier/0.jpg"

fun IVoiceChannel.getTrackPreferences(): Map<String, Int> {
    return mutableMapOf<String, Int>().apply {
        usersHere.asSequence()
                .filter { it != client.ourUser }
                .forEach { putAll(it.getTrackPreferences()) }
    }
}

fun IUser.isOnVoice(): Boolean {
    return RequestBuffer.request<Boolean> { voiceStates.values().any { it.channel != null } }.get()
}

fun IUser.isOnVoice(guild: IGuild): Boolean {
    return RequestBuffer.request<Boolean> { getVoiceStateForGuild(guild).channel != null }.get()
}

fun IUser.getConnectedVoiceChannel(): IVoiceChannel? {
    return RequestBuffer.request<IVoiceChannel?> { voiceStates.values().firstOrNull { it.channel != null }?.channel }.get()
}

fun IUser.getConnectedVoiceChannel(guild: IGuild): IVoiceChannel? {
    return RequestBuffer.request<IVoiceChannel?> { getVoiceStateForGuild(guild).channel }.get()
}

val IUser.privateChannel: IChannel get() = orCreatePMChannel

fun IUser.addRoles(roles: List<IRole>) = roles.forEach { request { addRole(it) } }

fun IUser.removeRoles(roles: List<IRole>) = roles.forEach { request { removeRole(it) } }

fun <T> request(block: () -> T) = RequestBuffer.request(block)!!

fun <T> requestGet(block: () -> T) = request(block).get()