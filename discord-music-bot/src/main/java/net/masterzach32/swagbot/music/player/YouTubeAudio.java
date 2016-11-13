/*
    SwagBot - A Discord Music Bot
    Copyright (C) 2016  Zachary Kozar

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc.,
    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package net.masterzach32.swagbot.music.player;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.masterzach32.swagbot.App;
import net.masterzach32.swagbot.utils.exceptions.FfmpegException;
import net.masterzach32.swagbot.utils.exceptions.YouTubeApiException;
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

    public YouTubeAudio(String url) throws UnirestException, YouTubeApiException {
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
                throw new YouTubeApiException(url);
            }
            response =  Unirest.get("https://www.googleapis.com/youtube/v3/videos" +
                    "?part=contentDetails" +
                    "&id=" + video_id +
                    "&key=" + App.prefs.getGoogleAuthKey()).asJson();
            if(response.getStatus() == 200) {
                try {
                    JSONObject json = response.getBody().getObject().getJSONArray("items").getJSONObject(0).getJSONObject("contentDetails");
                    duration = json.getString("duration");
                    if (json.has("regionRestriction")) {
                        json = json.getJSONObject("regionRestriction");
                        if (json.has("allowed")) {
                            JSONArray array = json.getJSONArray("allowed");
                            Iterator it = array.iterator();
                            boolean allowed = false;
                            while (it.hasNext() && !allowed)
                                if (it.next().equals("US"))
                                    allowed = true;
                            if (!allowed)
                                throw new YouTubeApiException(url);
                        } else if (json.has("blocked")) {
                            JSONArray array = json.getJSONArray("blocked");
                            for (Object obj : array)
                                if (obj.equals("US"))
                                    throw new YouTubeApiException(url);
                        } else
                            throw new YouTubeApiException(url);
                    }
                } catch (JSONException e){
                    throw new YouTubeApiException(url);
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

    public AudioTrack getAudioTrack(IUser user, boolean shouldAnnounce) throws IOException, UnsupportedAudioFileException, YouTubeDLException, FfmpegException {
        return new AudioTrack(new YouTubeAudioProvider(video_id), url, shouldAnnounce, title, user);
    }

    public boolean isLive() {
        return isLiveStream;
    }

    public boolean isDurationAnHour() {
        return duration.contains("H");
    }
}