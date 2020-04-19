package xyz.swagbot.features.music

import com.sedmelluq.discord.lavaplayer.track.*

val AudioTrackInfo.hasThumbnail: Boolean
    get() = uri.contains("youtu")

val AudioTrackInfo.thumbnailUrl: String
    get() = "https://img.youtube.com/vi/$identifier/0.jpg"

val AudioTrackInfo.formattedTitle: String
    get() = "$title by $author"

val AudioTrackInfo.boldFormattedTitle: String
    get() = "**$title** by **$author**"

val AudioTrack.trackContext: TrackContext
    get() = getUserData(TrackContext::class.java)

val AudioTrack.formattedPosition: String
    get() = getFormattedTime(position/1000)

val AudioTrack.formattedLength: String
    get() = getFormattedTime(duration/1000)

fun getFormattedTime(time: Long): String {
    val hours = time / 3600
    var remainder = time % 3600
    val minutes = remainder / 60
    remainder %= 60
    val seconds = remainder

    if (hours > 0)
        return String.format("%d:%02d:%02d", hours, minutes, seconds)
    return String.format("%d:%02d", minutes, seconds)
}
