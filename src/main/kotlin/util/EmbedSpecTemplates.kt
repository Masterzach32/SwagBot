package xyz.swagbot.util

import com.sedmelluq.discord.lavaplayer.track.*
import discord4j.core.spec.*
import xyz.swagbot.features.music.*
import java.time.*
import java.util.function.*

val baseTemplate: Consumer<EmbedCreateSpec> = Consumer { spec ->
    spec.setColor(BLUE)
}

val errorTemplate: Consumer<EmbedCreateSpec> = Consumer { spec ->
    spec.setColor(RED)
}

fun trackRequestedTemplate(requesterName: String, track: AudioTrack): Consumer<EmbedCreateSpec> = Consumer { spec ->
    spec.setColor(BLUE)
    spec.setTitle(":musical_note: | Track requested by $requesterName")
    spec.setDescription(
        """
                **[${track.info.title}](${track.info.uri})**
                Author/Channel: **${track.info.author}**
                Length: **${track.formattedLength}**
            """.trimIndent()
    )
    if (track.info.hasThumbnail)
        spec.setThumbnail(track.info.thumbnailUrl)

    spec.setTimestamp(Instant.now())
}