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
        App.logger.info("Soundcloud tracks are disabled.");
        streamUrl = "";
        author = "";
        id = "";
        /*try {
            HttpResponse<JsonNode> response = Unirest.get("http://api.soundcloud.com/resolve?url=" + url + "&client_id=" + App.prefs.getSCClientId())
                    .header("accept", "application/json")
                    .asJson();

            if(response.getStatus() != 200)
                App.logger.warn("Error with SoundCloud api: " + url);

            JSONObject json = response.getBody().getObject();

            title = json.getString("title");
            author = json.getJSONObject("user").getString("username");
            id = json.getInt("id") + "";
            if(!json.getBoolean("streamable"))
                throw new NotStreamableException(getSource(), url);
            else
                streamUrl = json.getString("stream_url");
        } catch (UnirestException e) {
            e.printStackTrace();
        }*/
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