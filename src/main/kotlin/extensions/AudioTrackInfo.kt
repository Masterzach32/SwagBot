package xyz.swagbot.extensions

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo

@Deprecated("")
val AudioTrackInfo.hasThumbnail: Boolean
    get() = uri.contains("youtu")

val AudioTrackInfo.thumbnailUrl: String?
    get() = if (uri.contains("youtu")) "https://img.youtube.com/vi/$identifier/0.jpg" else null

val AudioTrackInfo.formattedTitle: String
    get() = "$title by $author"

val AudioTrackInfo.formattedTitleWithLink: String
    get() = "[${title} by ${author}](${uri})"

val AudioTrackInfo.boldFormattedTitle: String
    get() = "**$title** by **$author**"

val AudioTrackInfo.boldFormattedTitleWithLink: String
    get() = "[**${title}** by **${author}**](${uri})"
