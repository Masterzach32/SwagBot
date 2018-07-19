package xyz.swagbot.api.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import kotlin.math.min

open class QueueBrowser(val tracks: List<AudioTrack>, val tracksPerPage: Int = 15) {

    private var pageIndex = 0

    fun getCurrentPage() = this[pageIndex]

    fun getNextPage() = if (hasNextPage()) this[pageIndex++] else getCurrentPage()

    fun hasNextPage() = pageIndex + 1 < pageCount()

    fun getPreviousPage() = if (hasPreviousPage()) this[pageIndex--] else getCurrentPage()

    fun hasPreviousPage() = pageIndex - 1 >= pageCount()

    fun pageCount() = tracks.size / tracksPerPage + 1

    fun isEmpty() = tracks.isEmpty()

    fun getPage(index: Int): Page {
        val startIndex = index * tracksPerPage
        return if (startIndex in tracks.indices)
            Page(index, tracks.subList(startIndex, min(startIndex + tracksPerPage, tracks.size)))
        else
            throw NoSuchElementException("Page $index doesn't exist.")
    }

    operator fun get(index: Int) = getPage(index)

    class Page internal constructor(val index: Int, val tracks: List<AudioTrack>)
}