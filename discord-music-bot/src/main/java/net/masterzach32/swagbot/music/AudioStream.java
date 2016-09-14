package net.masterzach32.swagbot.music;

import sx.blah.discord.handle.obj.IUser;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public class AudioStream implements AudioSource {

    private String url;

    public AudioStream(String url) {
        this.url = url;
    }

    public String getName() {
        return url;
    }

    public String getSource() {
        return "stream";
    }

    public String getUrl() {
        return url;
    }

    public String getStreamUrl() {
        return url;
    }

    public AudioTrack getAudioTrack(IUser user, boolean shouldAnnounce) {
        try {
            return new AudioTrack(url, shouldAnnounce, url, user);
        } catch (IOException | UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
        return null;
    }
}