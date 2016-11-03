package net.masterzach32.swagbot.api.jokes

import com.mashape.unirest.http.Unirest
import com.mashape.unirest.http.exceptions.UnirestException

class CNJoke : IRandomJoke {

    private var joke: String = ""

    init {
        try {
            val json = Unirest.get("https://api.icndb.com/jokes/random").asJson()
            joke = json.body.array.getJSONObject(0).getJSONObject("value").getString("joke")
        } catch (e: UnirestException) {
            e.printStackTrace()
        }

    }

    override fun getJoke(): String {
        return joke
    }
}