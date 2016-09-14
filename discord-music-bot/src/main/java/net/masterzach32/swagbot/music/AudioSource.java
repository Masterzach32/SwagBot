package net.masterzach32.swagbot.music;

import net.masterzach32.swagbot.utils.exceptions.FFMPEGException;
import net.masterzach32.swagbot.utils.exceptions.YouTubeDLException;
import sx.blah.discord.handle.obj.IUser;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public interface AudioSource {

    public String getName();

    public String getSource();

    public String getUrl();

    public String getStreamUrl();

    public AudioTrack getAudioTrack(IUser user, boolean shouldAnnounce) throws IOException, UnsupportedAudioFileException, YouTubeDLException, FFMPEGException;
}