package net.masterzach32.swagbot.api

import com.mashape.unirest.http.Unirest
import com.mashape.unirest.http.exceptions.UnirestException

class R8Ball {

    var response: String? = null
        private set

    init {
        try {
            response = Unirest.get("https://apis.rtainc.co/twitchbot/8ball").asString().body
        } catch (e: UnirestException) {
            e.printStackTrace()
        }

    }
}