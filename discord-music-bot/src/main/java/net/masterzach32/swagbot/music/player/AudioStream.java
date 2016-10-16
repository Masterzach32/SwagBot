package net.masterzach32.swagbot.music.player;

import sx.blah.discord.handle.obj.IUser;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public class AudioStream extends AudioSource {

    public AudioStream(String url) {
        this.url = url;
        this.source = "stream";
    }

    public String getTitle() {
        return url;
    }

    public String getUrl() {
        return url;
    }

    public String getStreamUrl() {
        return url;
    }

    public AudioTrack getAudioTrack(IUser user, boolean shouldAnnounce) throws IOException, UnsupportedAudioFileException {
        return new AudioTrack(url, shouldAnnounce, url, user);
    }
}