package xyz.swagbot.api

import com.mashape.unirest.http.Unirest
import org.json.JSONObject
import xyz.swagbot.database.getKey
import xyz.swagbot.logger
import java.net.URLEncoder

data class YouTubeVideo(val title: String, val channel: String, val id: String) {

    fun getUrl() = "https://youtube.com/watch?v=$id"
}

fun getVideoFromId(id: String): YouTubeVideo? {
    val response = Unirest.get("https://www.googleapis.com/youtube/v3/videos/?id=" +
            "${URLEncoder.encode(id, "UTF-8")}&part=snippet" +
            "&key=${getKey("google_auth_key")}").asJson()
    if (response.status != 200)
        return null
    val json = response.body.`object`

    if (json.has("items") && json.getJSONArray("items").length() > 0 &&
            isValidJSON(json.getJSONArray("items").getJSONObject(0)))
        return createVideoObject(json.getJSONArray("items").getJSONObject(0))
    return null
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
            if (isValidJSON(items.getJSONObject(i)))
                video = createVideoObject(items.getJSONObject(i))
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
            if (isValidJSON(items.getJSONObject(i)))
                list.add(createVideoObject(items.getJSONObject(i)))
            i++
        }
    }
    return list
}

private fun isValidJSON(json: JSONObject): Boolean {
    return json.getJSONObject("id").has("videoId")
}

private fun createVideoObject(json: JSONObject): YouTubeVideo {
    return YouTubeVideo(
            json.getJSONObject("snippet").getString("title"),
            json.getJSONObject("snippet").getString("channelTitle"),
            json.getJSONObject("id").getString("videoId")
    )
}