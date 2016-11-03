package net.masterzach32.swagbot.api

import com.mashape.unirest.http.Unirest
import com.mashape.unirest.http.exceptions.UnirestException
import net.masterzach32.swagbot.App
import org.json.JSONObject
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.util.RequestBuffer

class WordCloud {

    private var text: String? = null
    var url: String? = null
        private set

    @Throws(UnirestException::class)
    constructor(channel: IChannel) {
        val list = channel.messages
        text = ""
        do {
            for (i in list.indices)
                text += list[i].content + " "
            RequestBuffer.request { list.load(100) }
        } while (!list.isEmpty())
        getWordCloudFromText()
    }

    @Throws(UnirestException::class)
    constructor(str: String) {
        this.text = str
        getWordCloudFromText()
    }

    @Throws(UnirestException::class)
    private fun getWordCloudFromText() {
        val obj = JSONObject().put("f_type", "png").put("width", 800).put("height", 500).put("s_max", "7").put("s_min", "1").put("f_min", 1).put("r_color", "TRUE").put("r_order", "TRUE").put("s_fit", "FALSE").put("fixed_asp", "TRUE").put("rotate", "TRUE").put("textblock", text)
        val response = Unirest.post("https://wordcloudservice.p.mashape.com/generate_wc").header("X-Mashape-Key", App.prefs.mashapApiKey).header("Content-Type", "application/json").header("Accept", "application/json").body(obj).asJson()
        if (response.status != 200) {
            App.logger.info("Received response code " + response.status + " for WordCloud API:\n" + response.body)
            url = "Could not make word cloud: " + response.statusText
        } else {
            url = response.body.array.getJSONObject(0).getString("url")
        }
    }
}