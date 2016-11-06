package net.masterzach32.swagbot.api

import com.mashape.unirest.http.Unirest
import com.mashape.unirest.http.exceptions.UnirestException
import org.json.JSONObject

class URLShortener(url: String) {

    var url: String? = null
        private set

    init {
        try {
            val obj = JSONObject()
            obj.put("longUrl", url)
            val response = Unirest.post("https://www.googleapis.com/urlshortener/v1/url?key=" + App.prefs.googleAuthKey).header("Content-Type", "application/json").body(obj.toString()).asJson()
            if (response.status != 200)
                App.logger.info("Google API responded with status code " + response.status + ": " + response.statusText)
            else
                this.url = response.body.`object`.getString("id")
        } catch (e: UnirestException) {
            e.printStackTrace()
        }

    }
}