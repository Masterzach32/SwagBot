package xyz.swagbot.api.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack

class QueueBrowser(tracks: List<AudioTrack>, val tracksPerPage: Int = 15) {

    private val pages: Array<Page>

    private var pageIndex = 0

    init {
        val tracksIn = tracks.toMutableList()
        val pageList = mutableListOf<Page>()
        var pageIndex = 0
        while (tracksIn.isNotEmpty()) {
            val pageTracks = mutableListOf<AudioTrack>()
            when {
                tracksIn.size >= 15 -> for (i in 0 until 15) pageTracks.add(tracksIn.removeAt(i))
                tracksIn.size < 15 -> tracksIn.apply { pageTracks.addAll(this) }.clear()
            }
            pageList.add(Page(pageIndex++, pageTracks))
        }
        pages = pageList.toTypedArray()
    }

    fun getCurrentPage() = pages[pageIndex]

    fun getNextPage() = if (hasNextPage()) pages[pageIndex++] else pages.last()

    fun hasNextPage() = pageIndex + 1 < pages.size

    fun getPreviousPage() = if (hasPreviousPage()) pages[pageIndex--] else pages.first()

    fun hasPreviousPage() = pageIndex - 1 >= pages.size

    fun pageCount() = pages.size

    fun isEmpty() = pageCount() == 0

    fun getPage(index: Int): Page? = if (index in pages.indices) pages[index] else null

    class Page internal constructor(val index: Int, val tracks: List<AudioTrack>)
}