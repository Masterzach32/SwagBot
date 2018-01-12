package xyz.swagbot.api

import com.mashape.unirest.http.Unirest
import xyz.swagbot.database.getKey
import xyz.swagbot.logger
import java.net.URLEncoder

class UrbanDefinition(term: String) {
    private var defid: Int = 0
    private var hasEntry: Boolean = false
    var term: String? = null
        private set
    var author: String? = null
        private set
    var definition: String? = null
        private set
    var example: String? = null
        private set
    var link: String? = null
        private set

    init {
        try {
            val response = Unirest.get("https://mashape-community-urban-dictionary.p.mashape.com/define?term=" +
                    URLEncoder.encode(term, "UTF-8")).header("X-Mashape-Key",
                    getKey("mashap_auth_key")).header("Accept", "text/plain").asJson()
            if (response.status != 200) {
                logger.warn("Urban Dictionary responded with status code " + response.status + ": " + response.statusText)
            }
            var def = response.body.array.getJSONObject(0)
            if (def.getJSONArray("list").length() == 0) {
                hasEntry = false
                this.term = term
            } else {
                hasEntry = true
                def = def.getJSONArray("list").getJSONObject(0)
                this.defid = def.getInt("defid")
                this.term = def.getString("word")
                this.author = def.getString("author")
                this.definition = def.getString("definition")
                this.example = def.getString("example")
                this.link = def.getString("permalink")
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }

    }

    fun getdefid(): Int {
        return defid
    }

    fun hasEntry(): Boolean {
        return hasEntry
    }
}