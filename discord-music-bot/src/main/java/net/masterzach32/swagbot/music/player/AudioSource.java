package net.masterzach32.swagbot.music.player;

import net.masterzach32.swagbot.utils.exceptions.FFMPEGException;
import net.masterzach32.swagbot.utils.exceptions.YouTubeDLException;
import org.json.JSONObject;
import sx.blah.discord.handle.obj.IUser;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public abstract class AudioSource {

    protected String title, url, source;

    public abstract String getTitle();

    public String getSource() {
        return source;
    }

    public abstract String getUrl();

    public abstract String getStreamUrl();

    public abstract AudioTrack getAudioTrack(IUser user, boolean shouldAnnounce) throws IOException, UnsupportedAudioFileException, YouTubeDLException, FFMPEGException;

    public JSONObject saveToJson() {
        JSONObject obj = new JSONObject();
        obj.put("title", title);
        obj.put("url", url);
        obj.put("source", source);
        return obj;
    }
}