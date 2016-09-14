package net.masterzach32.swagbot.music;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.sound.sampled.UnsupportedAudioFileException;

import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.audio.AudioPlayer.Track;
import sx.blah.discord.util.audio.providers.FileProvider;
import sx.blah.discord.util.audio.providers.URLProvider;

public class AudioTrack extends Track {
	
	private boolean announce;
	private String title;
	private IUser user;

	public AudioTrack(File file, boolean announce, String title, IUser user) throws IOException, UnsupportedAudioFileException {
		super(new FileProvider(file));
		this.announce = announce;
		this.title = title;
		this.user = user;
	}

	public AudioTrack(String stream, boolean announce, String title, IUser user) throws IOException, UnsupportedAudioFileException {
		super(new URLProvider(new URL(stream)));
		this.announce = announce;
		this.title = title;
		this.user = user;
	}
	
	public String getTitle() {
		return title;
	}
	
	public IUser getUser() {
		return user;
	}
	
	public boolean shouldAnnounce() {
		return announce;
	}
}