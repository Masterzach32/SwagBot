package net.masterzach32.swagbot.api

import org.json.JSONObject

import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.JsonNode
import com.mashape.unirest.http.Unirest
import com.mashape.unirest.http.exceptions.UnirestException
import net.masterzach32.swagbot.App

class RandomQuote(cat: String) {

    var quote: String? = null
        private set
    var author: String? = null
        private set
    var category: String? = null
        private set

    init {
        try {
            val response = Unirest.post("https://andruxnet-random-famous-quotes.p.mashape.com/?cat=" + cat).header("X-Mashape-Key", App.prefs.mashapApiKey).header("Content-Type", "application/x-www-form-urlencoded").header("Accept", "application/json").asJson()
            val json = response.body.array.getJSONObject(0)
            quote = json.getString("quote")
            author = json.getString("author")
            category = json.getString("category")
        } catch (e: UnirestException) {
            e.printStackTrace()
        }

    }
}