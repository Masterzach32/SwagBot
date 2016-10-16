package net.masterzach32.swagbot.music.player;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.masterzach32.swagbot.App;
import net.masterzach32.swagbot.utils.exceptions.NotStreamableException;
import org.json.JSONObject;
import sx.blah.discord.handle.obj.IUser;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public class SoundCloudAudio extends AudioSource {

    private String streamUrl, author, id;

    public SoundCloudAudio(String url) throws NotStreamableException {
        this.url = url;
        this.source = "soundcloud";
        try {
            HttpResponse<JsonNode> response = Unirest.get("http://api.soundcloud.com/resolve?url=" + url + "&client_id=" + App.prefs.getSCClientId())
                    .header("Content-Type", "application/json")
                    .asJson();
            JSONObject json = response.getBody().getArray().getJSONObject(0);

            if(response.getStatus() != 200)
                App.logger.warn("Error with SoundCloud api: " + url);

            title = json.getString("title");
            author = json.getJSONObject("user").getString("username");
            id = json.getInt("id") + "";
            if(!json.getBoolean("streamable"))
                throw new NotStreamableException(getSource(), url);
            else
                streamUrl = json.getString("stream_url");
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }

    public String getTitle() {
        return title + " - " + author;
    }

    public String getUrl() {
        return url;
    }

    public String getStreamUrl() {
        return streamUrl + "?client_id=" + App.prefs.getSCClientId();
    }

    public AudioTrack getAudioTrack(IUser user, boolean shouldAnnounce) throws IOException, UnsupportedAudioFileException {
        return new AudioTrack(getStreamUrl(), shouldAnnounce, getTitle(), user);
    }
}