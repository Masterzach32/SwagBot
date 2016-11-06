package net.masterzach32.swagbot.api

import org.json.JSONObject

import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.JsonNode
import com.mashape.unirest.http.Unirest
import com.mashape.unirest.http.exceptions.UnirestException
import net.masterzach32.swagbot.App

import net.masterzach32.swagbot.EventHandler
import sx.blah.discord.handle.obj.IGuild

class NSFWFilter(url: String) {

    private var raw: Double = 0.toDouble()
    private var partial: Double = 0.toDouble()
    private var safe: Double = 0.toDouble()
    var url: String? = null
        private set
    var status: String? = null
        private set

    init {
        val api_user = App.prefs.apiUser
        val api_secret = App.prefs.apiSecret
        try {
            val response = Unirest.get("https://api.sightengine.com/1.0/nudity.json?api_user=$api_user&api_secret=$api_secret&url=$url").asJson()
            val result = response.body.array.getJSONObject(0)
            if (result.getString("status") == "failure")
                EventHandler.logger.info(result.toString())
            else {
                val nudity = result.getJSONObject("nudity")
                status = result.getString("status")
                raw = nudity.getDouble("raw")
                partial = nudity.getDouble("partial")
                safe = nudity.getDouble("safe")
                this.url = url
            }

        } catch (e: UnirestException) {
            e.printStackTrace()
        }

    }

    val isPartial: Boolean
        get() = partial > .4

    val isNSFW: Boolean
        get() = raw > .4

    val isSafe: Boolean
        get() = safe > .5

    fun getRaw(): Double {
        return raw * 100
    }

    fun getPartial(): Double {
        return partial * 100
    }

    fun getSafe(): Double {
        return safe * 100
    }
}