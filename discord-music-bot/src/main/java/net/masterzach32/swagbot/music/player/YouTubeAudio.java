package net.masterzach32.swagbot.music.player;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.masterzach32.swagbot.App;
import net.masterzach32.swagbot.utils.exceptions.FFMPEGException;
import net.masterzach32.swagbot.utils.exceptions.YouTubeAPIException;
import net.masterzach32.swagbot.utils.exceptions.YouTubeDLException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sx.blah.discord.handle.obj.IUser;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.util.Iterator;

public class YouTubeAudio extends AudioSource {

    private String video_id, duration;
    private boolean isLiveStream;

    public YouTubeAudio(String url) throws UnirestException, YouTubeAPIException {
        this.url = url;
        this.source = "youtube";
        video_id = url.substring(url.indexOf("?v=") + 3, url.indexOf("=") + 12);
        HttpResponse<JsonNode> response;

        response =  Unirest.get("https://www.googleapis.com/youtube/v3/videos" +
                "?part=snippet" +
                "&id=" + video_id +
                "&key=" + App.prefs.getGoogleAuthKey()).asJson();
        if(response.getStatus() == 200) {
            try {
                JSONObject json = response.getBody().getObject().getJSONArray("items").getJSONObject(0).getJSONObject("snippet");
                title = json.getString("title");
                isLiveStream = json.getString("liveBroadcastContent").equals("live");
            } catch (JSONException e){
                throw new YouTubeAPIException(url);
            }
            response =  Unirest.get("https://www.googleapis.com/youtube/v3/videos" +
                    "?part=contentDetails" +
                    "&id=" + video_id +
                    "&key=" + App.prefs.getGoogleAuthKey()).asJson();
            if(response.getStatus() == 200) {
                try {
                    JSONObject json = response.getBody().getObject().getJSONArray("items").getJSONObject(0).getJSONObject("contentDetails");
                    duration = json.getString("duration");
                    JSONArray array = json.getJSONObject("regionRestriction").getJSONArray("blocked");
                    Iterator it = array.iterator();
                    while (it.hasNext())
                        if(it.next().equals("US"))
                            throw new YouTubeAPIException(url);

                } catch (JSONException e){
                    throw new YouTubeAPIException(url);
                }
            } else
                App.logger.warn("Youtube Data API responded with status code " + response.getStatus() + " for video id " + video_id);
        } else
            App.logger.warn("Youtube Data API responded with status code " + response.getStatus() + " for video id " + video_id);

    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public String getStreamUrl() {
        return null;
    }

    public AudioTrack getAudioTrack(IUser user, boolean shouldAnnounce) throws IOException, UnsupportedAudioFileException, YouTubeDLException, FFMPEGException {
        return new AudioTrack(new YouTubeAudioProvider(video_id), url, shouldAnnounce, title, user);
    }

    public boolean isLive() {
        return isLiveStream;
    }

    public boolean isDurationAnHour() {
        return duration.contains("H");
    }
}