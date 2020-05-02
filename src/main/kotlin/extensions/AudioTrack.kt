package xyz.swagbot.extensions

import com.sedmelluq.discord.lavaplayer.track.*
import xyz.swagbot.features.music.*

val AudioTrack.trackContext: TrackContext
    get() = getUserData(TrackContext::class.java)

val AudioTrack.formattedPosition: String
    get() = getFormattedTime(position/1000)

val AudioTrack.formattedLength: String
    get() = getFormattedTime(duration/1000)
