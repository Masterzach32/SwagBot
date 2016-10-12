package net.masterzach32.swagbot.api;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.masterzach32.swagbot.App;
import org.json.JSONObject;

public class URLShortener {

    private String url;

    public URLShortener(String url) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("longUrl", url);
            HttpResponse<JsonNode> response = Unirest.post("https://www.googleapis.com/urlshortener/v1/url?key=" + App.prefs.getGoogleAuthKey())
                    .header("Content-Type", "application/json")
                    .body(obj.toString())
                    .asJson();
            if(response.getStatus() != 200)
                App.logger.info("Google API responded with status code " + response.getStatus() + ": " + response.getStatusText());
            else
                this.url = response.getBody().getObject().getString("id");
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }

    public String getUrl() {
        return url;
    }
}