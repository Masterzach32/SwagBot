package net.masterzach32.swagbot.api.jokes

import com.mashape.unirest.http.Unirest
import com.mashape.unirest.http.exceptions.UnirestException

class YMJoke: IRandomJoke {

    private var joke: String = ""

    init {
        try {
            val json = Unirest.get("http://api.yomomma.info/").asJson()
            joke = json.body.array.getJSONObject(0).getString("joke")
        } catch (e: UnirestException) {
            e.printStackTrace()
        }

    }

    override fun getJoke(): String {
        return joke
    }
}