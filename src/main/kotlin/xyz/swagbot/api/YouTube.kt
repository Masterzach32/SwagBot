package xyz.swagbot.api

import com.mashape.unirest.http.Unirest
import org.json.JSONObject
import xyz.swagbot.database.getKey
import xyz.swagbot.database.logger
import java.net.URLEncoder

data class YouTubeVideo(val title: String, val channel: String, val identifier: String) {

    fun getUrl() = "https://youtube.com/watch?v=$identifier"
}

fun getIdFromSearch(search: String): String? {
    val response = Unirest.get("https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=50&q=" +
            URLEncoder.encode(search, "UTF-8") + "&key=" + getKey("google_auth_key")).asJson()
    if (response.status != 200)
        return null
    val json: JSONObject
    json = response.body.array.getJSONObject(0)
    if (json.has("items") && json.getJSONArray("items").length() > 0 &&
            json.getJSONArray("items").getJSONObject(0).has("id") &&
            json.getJSONArray("items").getJSONObject(0).getJSONObject("id").has("videoId"))
        return json.getJSONArray("items").getJSONObject(0).getJSONObject("id").getString("videoId")
    return null
}

fun getIdSetFromSearch(search: String, size: Int): Set<String> {
    val response = Unirest.get("https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=50&q=" +
            URLEncoder.encode(search, "UTF-8") + "&key=" + getKey("google_auth_key")).asJson()
    if (response.status != 200)
        return emptySet()
    val json = response.body.array.getJSONObject(0)
    if (json.has("items") && json.getJSONArray("items").length() > 0)
        return (0..(size-1))
                .map { json.getJSONArray("items").getJSONObject(it).getJSONObject("id").getString("videoId") }
                .toSet()
    return emptySet()
}

fun getVideoFromSearch(search: String): YouTubeVideo? {
    val id = getIdFromSearch(search) ?: return null
    return getVideoFromId(id)
}

fun getVideoSetFromSearch(search: String, size: Int): List<YouTubeVideo> {
    val idSet = getIdSetFromSearch(search, size)
    val list = mutableListOf<YouTubeVideo>()
    idSet.forEach {
        try {
            val video = getVideoFromId(it) ?: throw NullPointerException("Could not load video from id: $it")
            list.add(video)
        } catch (t: Throwable) {
            t.printStackTrace()
        }
        Thread.sleep(200)
    }
    return list
}

fun getVideoFromId(id: String): YouTubeVideo? {
    try {
        val apiCall = "https://www.googleapis.com/youtube/v3/videos?part=snippet&id=$id"
        val response = Unirest.get("$apiCall&key=${getKey("google_auth_key")}").asJson()
        val title = response.body.`object`.getJSONArray("items").getJSONObject(0)
                .getJSONObject("snippet").getString("title")
        val channel = response.body.`object`.getJSONArray("items").getJSONObject(0)
                .getJSONObject("snippet").getString("channelTitle")
        return YouTubeVideo(title, channel, id)
    } catch (t: Throwable) {
        t.printStackTrace()
    }
    return null
}