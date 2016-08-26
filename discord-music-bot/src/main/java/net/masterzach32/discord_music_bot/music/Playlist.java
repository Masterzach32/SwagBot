package net.masterzach32.discord_music_bot.music;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.sound.sampled.UnsupportedAudioFileException;

import net.masterzach32.discord_music_bot.App;
import sx.blah.discord.handle.obj.IGuild;

public class Playlist {
	
	private String name;
	private List<String> music;

	public Playlist(String name) {
		this.name = name;
		music = new ArrayList<String>();
	}
	
	public void add(String audio) {
		music.add(audio);
	}
	
	public void remove(String audio) {
		for(String s : music)
			if(s.equals(audio))
				music.remove(s);
	}
	
	public String getName() {
		return name;
	}
	
	public void queue(IGuild guild) {
		Collections.shuffle(music);
		for(String s : music)
			try {
				App.playAudioFromYouTube(s, guild);
			} catch (IOException | UnsupportedAudioFileException e) {
				e.printStackTrace();
			}
	}
	
	public String getInfo() {
		String str = "";
		for(int i = 0; i < music.size(); i++)
			str += (i+1) + ". " + music.get(i) + "\n";
		return str;
	}
}