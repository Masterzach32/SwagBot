package net.masterzach32.discord_music_bot.music;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.UnsupportedAudioFileException;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.audio.AudioPlayer.Track;
import sx.blah.discord.util.audio.providers.FileProvider;

public class AudioTrack extends Track {
	
	private String title;
	private IUser user;
	private IChannel channel;

	public AudioTrack(File file, String title, IUser user, IChannel channel) throws IOException, UnsupportedAudioFileException {
		super(new FileProvider(file));
		this.title = title;
		this.user = user;
		this.channel = channel;
	}
	
	public String getTitle() {
		return title;
	}
	
	public IUser getUser() {
		return user;
	}
	
	public IChannel getChannel() {
		return channel;
	}
}