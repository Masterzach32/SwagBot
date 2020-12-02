package xyz.swagbot.features.music

import com.sedmelluq.discord.lavaplayer.track.*
import io.facet.discord.dsl.*
import xyz.swagbot.extensions.*
import xyz.swagbot.util.*

fun notPremiumTemplate(
    prefixUsed: String
): MessageTemplate = message {
    content = "Music commands are a premium feature of SwagBot. Type `${prefixUsed}premium` to learn more."
}

fun trackRequestedTemplate(
    requesterName: String,
    track: AudioTrack,
    timeUntilPlayed: Long = 0
): EmbedTemplate = baseTemplate.andThen {
    title = ":musical_note: | Track requested by $requesterName"
    description = """
        **[${track.info.title}](${track.info.uri})**
        Author/Channel: **${track.info.author}**
        Length: **${track.formattedLength}**
        ${if (timeUntilPlayed > 0) "Estimated time until played: **${getFormattedTime(timeUntilPlayed / 1000)}**" else ""}
        """.trimIndent().trim()

    if (track.info.thumbnailUrl != null)
        thumbnailUrl = track.info.thumbnailUrl!!

    //timestamp = Instant.now()
}

fun trackSkippedTemplate(
    requesterName: String,
    track: AudioTrack,
    nextTrack: AudioTrack?
): EmbedTemplate = baseTemplate.andThen {
    title = ":track_next: | Track skipped by $requesterName"
    description = """
        ${track.info.boldFormattedTitleWithLink}
        ${if (nextTrack != null) "Up next: ${nextTrack.info.boldFormattedTitleWithLink}" else ""}
    """.trimIndent().trim()

    if (track.info.thumbnailUrl != null)
        thumbnailUrl = track.info.thumbnailUrl!!

    //timestamp = Instant.now()
}
