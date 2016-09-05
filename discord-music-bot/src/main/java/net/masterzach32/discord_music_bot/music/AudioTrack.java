package net.masterzach32.discord_music_bot.music;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.UnsupportedAudioFileException;

import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.audio.AudioPlayer.Track;
import sx.blah.discord.util.audio.providers.FileProvider;

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