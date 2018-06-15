package xyz.swagbot.api

import com.mashape.unirest.http.Unirest
import xyz.swagbot.database.getKey
import xyz.swagbot.logger
import java.net.URLEncoder

data class UrbanDefinition(
        val defID: Int,
        val word: String,
        val author: String,
        val definition: String,
        val example: String,
        val permalink: String
) {
    companion object {
        fun getDefinition(search: String): UrbanDefinition? {
            try {
                val response = Unirest.get(
                        "https://mashape-community-urban-dictionary.p.mashape.com/define?term=" +
                                URLEncoder.encode(search, "UTF-8")
                ).header("X-Mashape-Key", getKey("mashap_auth_key")).header("Accept", "text/plain").asJson()

                if (response.status != 200) {
                    logger.warn("Urban Dictionary responded with status code " + response.status + ": " + response.statusText)
                    return null
                }

                val defObj = response.body.array.getJSONObject(0)
                println(defObj.toString(2))
                if (defObj.getJSONArray("list").length() == 0)
                    return null

                val def = defObj.getJSONArray("list").getJSONObject(0)

                val defid = def.getInt("defid")
                val word = def.getString("word")
                val author = def.getString("author")
                val definition = def.getString("definition")
                val example = def.getString("example")
                val permalink = def.getString("permalink")

                return UrbanDefinition(defid, word, author, definition, example, permalink)
            } catch (t: Throwable) {
                t.printStackTrace()
            }

            return null
        }
    }
}
