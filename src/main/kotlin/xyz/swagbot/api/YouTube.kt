package xyz.swagbot.api

import com.mashape.unirest.http.Unirest
import org.json.JSONObject
import xyz.swagbot.database.getKey
import java.net.URLEncoder

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

fun getVideoFromSearch(search: String): String? {
    val id = getIdFromSearch(search) ?: return null
    return "https://youtube.com/watch?v=$id"
}