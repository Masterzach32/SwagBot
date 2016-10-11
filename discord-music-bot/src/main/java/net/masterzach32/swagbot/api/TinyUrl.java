package net.masterzach32.swagbot.api;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.masterzach32.swagbot.App;

public class TinyUrl {

    private String url;

    public TinyUrl(String url) {
        try {
            HttpResponse<String> response = Unirest.get("http://tinyurl.com/api-create.php?url=" + url).asString();
            if(response.getStatus() != 200)
                App.logger.info("TinyURL API responded with status code " + response.getStatus());
            else
                this.url = response.getBody();
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }

    public String getUrl() {
        return url;
    }
}