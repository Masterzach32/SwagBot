package xyz.swagbot.api

import com.mashape.unirest.http.Unirest
import org.json.JSONObject
import xyz.swagbot.database.getKey
import xyz.swagbot.database.logger
import java.net.URLEncoder

data class YouTubeVideo(val title: String, val channel: String, val identifier: String) {

    fun getUrl() = "https://youtube.com/watch?v=$identifier"
}

fun getVideoFromSearch(search: String): YouTubeVideo? {
    val response = Unirest.get("https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=50&q=" +
            URLEncoder.encode(search, "UTF-8") + "&key=" + getKey("google_auth_key")).asJson()
    if (response.status != 200)
        return null
    val json = response.body.`object`
    var video: YouTubeVideo? = null

    if (json.has("items") && json.getJSONArray("items").length() > 0) {
        val items = json.getJSONArray("items")
        var i = 0
        while (video == null && i < items.length()) {
            if (items.getJSONObject(i).getJSONObject("id").has("videoId")) {
                val id = json.getJSONArray("items").getJSONObject(i).getJSONObject("id").getString("videoId")
                val title = json.getJSONArray("items").getJSONObject(i).getJSONObject("snippet").getString("title")
                val channel = json.getJSONArray("items").getJSONObject(i).getJSONObject("snippet").getString("channelTitle")
                video = YouTubeVideo(title, channel, id)
            }
            i++
        }
    }

    return video
}

fun getVideoSetFromSearch(search: String, size: Int): List<YouTubeVideo> {
    val list = mutableListOf<YouTubeVideo>()
    val response = Unirest.get("https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=50&q=" +
            URLEncoder.encode(search, "UTF-8") + "&key=" + getKey("google_auth_key")).asJson()
    if (response.status != 200)
        return emptyList()
    val json = response.body.`object`
    if (json.has("items") && json.getJSONArray("items").length() > 0) {
        val items = json.getJSONArray("items")
        var i = 0
        while (list.size < size && i < items.length()) {
            if (items.getJSONObject(i).getJSONObject("id").has("videoId")) {
                val id = json.getJSONArray("items").getJSONObject(i).getJSONObject("id").getString("videoId")
                val title = json.getJSONArray("items").getJSONObject(i).getJSONObject("snippet").getString("title")
                val channel = json.getJSONArray("items").getJSONObject(i).getJSONObject("snippet").getString("channelTitle")
                list.add(YouTubeVideo(title, channel, id))
            }
            i++
        }
    }
    return list
}