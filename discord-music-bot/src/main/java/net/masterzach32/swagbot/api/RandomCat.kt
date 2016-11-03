package net.masterzach32.swagbot.api

import com.mashape.unirest.http.Unirest
import com.mashape.unirest.http.exceptions.UnirestException

class RandomCat {

    var url: String? = null
        private set

    init {
        try {
            val json = Unirest.get("http://random.cat/meow").asJson()
            url = json.body.array.getJSONObject(0).getString("file")
        } catch (e: UnirestException) {
            e.printStackTrace()
        }

    }
}