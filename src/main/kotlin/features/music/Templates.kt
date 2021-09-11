package xyz.swagbot.features.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import discord4j.core.spec.EmbedCreateSpec
import discord4j.core.spec.MessageCreateSpec
import io.facet.common.dsl.and
import io.facet.common.dsl.message
import xyz.swagbot.extensions.boldFormattedTitleWithLink
import xyz.swagbot.extensions.formattedLength
import xyz.swagbot.extensions.thumbnailUrl
import xyz.swagbot.util.baseTemplate

fun notPremiumTemplate(
    prefixUsed: String
): MessageCreateSpec = message {
    content = "Music commands are a premium feature of SwagBot. Type `${prefixUsed}premium` to learn more."
}

fun trackRequestedTemplate(
    requesterName: String,
    track: AudioTrack,
    timeUntilPlayed: Long = 0
): EmbedCreateSpec = baseTemplate.and {
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
): EmbedCreateSpec = baseTemplate.and {
    title = ":track_next: | Track skipped by $requesterName"
    description = """
        ${track.info.boldFormattedTitleWithLink}
        ${if (nextTrack != null) "Up next: ${nextTrack.info.boldFormattedTitleWithLink}" else ""}
    """.trimIndent().trim()

    if (track.info.thumbnailUrl != null)
        thumbnailUrl = track.info.thumbnailUrl!!

    //timestamp = Instant.now()
}
