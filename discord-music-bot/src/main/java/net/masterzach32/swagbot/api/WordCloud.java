package net.masterzach32.swagbot.api;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.masterzach32.swagbot.App;
import org.json.JSONObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.MessageList;
import sx.blah.discord.util.RequestBuffer;

public class WordCloud {

    private String text, url;

    public WordCloud(IChannel channel) throws UnirestException {
        MessageList list = channel.getMessages();
        text = "";
        do {
            for(int i = 0; i < list.size(); i++)
                text += list.get(i).getContent() + " ";
            RequestBuffer.request(() -> list.load(100));
        } while(!list.isEmpty());
        getWordCloudFromText();
    }

    public WordCloud(String str) throws UnirestException {
        this.text = str;
        getWordCloudFromText();
    }

    private void getWordCloudFromText() throws UnirestException {
        JSONObject obj = new JSONObject()
                .put("f_type", "png")
                .put("width", 800)
                .put("height", 500)
                .put("s_max", "7")
                .put("s_min", "1")
                .put("f_min", 1)
                .put("r_color", "TRUE")
                .put("r_order", "TRUE")
                .put("s_fit", "FALSE")
                .put("fixed_asp", "TRUE")
                .put("rotate", "TRUE")
                .put("textblock", text);
        HttpResponse<JsonNode> response = Unirest.post("https://wordcloudservice.p.mashape.com/generate_wc")
                .header("X-Mashape-Key", App.prefs.getMashapApiKey())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body(obj)
                .asJson();
        if(response.getStatus() != 200) {
            App.logger.info("Received response code " + response.getStatus() + " for WordCloud API:\n" + response.getBody());
            url = "Could not make word cloud: " + response.getStatusText();
        } else {
            url = response.getBody().getArray().getJSONObject(0).getString("url");
        }
    }

    public String getUrl() {
        return url;
    }
}