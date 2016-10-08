package net.masterzach32.swagbot.music;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sound.sampled.UnsupportedAudioFileException;

import com.mashape.unirest.http.exceptions.UnirestException;
import net.masterzach32.swagbot.App;
import net.masterzach32.swagbot.music.player.AudioSource;
import net.masterzach32.swagbot.music.player.AudioStream;
import net.masterzach32.swagbot.music.player.SoundCloudAudio;
import net.masterzach32.swagbot.music.player.YouTubeAudio;
import net.masterzach32.swagbot.utils.exceptions.NotStreamableException;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

public class LocalPlaylist {
	
	private String name;
	private boolean locked, requiresPerms;
	private List<String> music;

	public LocalPlaylist(String name, boolean locked, boolean requiresPerms) {
		this.name = name;
		this.locked = locked;
		this.requiresPerms = requiresPerms;
		music = new ArrayList<>();
	}

	public LocalPlaylist(String name, List<String> music, boolean locked, boolean requiresPerms) throws UnirestException {
		this.name = name;
		this.locked = locked;
		this.requiresPerms = requiresPerms;
		this.music = music;
	}
	
	public boolean add(String audio) {
		if(music.contains(audio))
			return false;
        music.add(audio);
		return true;
	}
	
	public void remove(String audio) {
		for(String s : music)
			if(s.equals(audio))
				music.remove(s);
	}
	
	public String getName() {
		return name;
	}
	
	public void queue(IUser user, IGuild guild) {
		Collections.shuffle(music);
		ExecutorService executor = Executors.newFixedThreadPool(3);
		for(String s : music) {
			Thread task = new Thread("loadAudioFromPlaylist:" + s) {
				public void run() {
					try {
						AudioSource source = null;
						try {
							if(s.contains("youtube"))
								source = new YouTubeAudio(s);
							else if(s.contains("soundcloud"))
								source = new SoundCloudAudio(s);
							else
								source = new AudioStream(s);
						} catch (NotStreamableException | UnirestException e) {
							e.printStackTrace();
						}
						App.playAudioFromAudioSource(source, true, null, user, guild);
					} catch (IOException | UnsupportedAudioFileException e) {
						e.printStackTrace();
					}
				}
			};
			executor.execute(task);
		}
	}
	
	public String getInfo() {
		String str = "";
		for(int i = 0; i < music.size(); i++) {
            AudioSource source = null;
            try {
                if(music.get(i).contains("youtube"))
                    source = new YouTubeAudio(music.get(i));
                else if(music.get(i).contains("soundcloud"))
                    source = new SoundCloudAudio(music.get(i));
                else
                    source = new AudioStream(music.get(i));
            } catch (NotStreamableException | UnirestException e) {
                e.printStackTrace();
            }
			str += "" + (i + 1) + ". **" + source.getTitle() + "**\n";
		}
        str += "";
		return str;
	}
	
	public int songs() {
		return music.size();
	}
	
	public boolean isLocked() {
		return locked;
	}
	
	public void toggleLocked() {
		this.locked = !locked;
	}
	
	public boolean requiresPerms() {
		return requiresPerms;
	}
	
	public void toggleRequiresPerms() {
		this.requiresPerms = !requiresPerms;
	}
}