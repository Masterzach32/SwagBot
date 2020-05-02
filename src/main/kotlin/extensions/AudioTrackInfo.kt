package xyz.swagbot.extensions

import com.sedmelluq.discord.lavaplayer.track.*

val AudioTrackInfo.hasThumbnail: Boolean
    get() = uri.contains("youtu")

val AudioTrackInfo.thumbnailUrl: String
    get() = "https://img.youtube.com/vi/$identifier/0.jpg"

val AudioTrackInfo.formattedTitle: String
    get() = "$title by $author"

val AudioTrackInfo.boldFormattedTitle: String
    get() = "**$title** by **$author**"
