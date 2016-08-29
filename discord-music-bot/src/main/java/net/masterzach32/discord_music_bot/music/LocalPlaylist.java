package net.masterzach32.discord_music_bot.music;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sound.sampled.UnsupportedAudioFileException;

import net.masterzach32.discord_music_bot.App;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

public class LocalPlaylist {
	
	private String name;
	private List<String> music;

	public LocalPlaylist(String name) {
		this.name = name;
		music = new ArrayList<String>();
	}
	
	public boolean add(String audio) {
		String str = "";
		if(audio.indexOf("?v=") < 0)
    		str = audio;
    	else 
    		str = audio.substring(audio.indexOf("?v=") + 3, audio.indexOf("=") + 12);
		if(music.contains(str))
			return false;
		
		music.add(str);
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
	
	public void queue(IUser user, IChannel channel, IGuild guild) {
		Collections.shuffle(music);
		ExecutorService executor = Executors.newFixedThreadPool(3);
		for(String s : music) {
			Thread task = new Thread("loadAudioFromPlaylist:" + s) {
				public void run() {
					try {
						App.playAudioFromYouTube(s, user, channel, guild);
					} catch (IOException | UnsupportedAudioFileException | InterruptedException e) {
						e.printStackTrace();
					}
				}
			};
			executor.execute(task);
		}
	}
	
	public String getInfo() {
		String str = "";
		for(int i = 0; i < music.size(); i++)
			str += "**" + (i+1) + ".** " + music.get(i) + "\n";
		return str;
	}
	
	public int songs() {
		return music.size();
	}
}