package net.masterzach32.discord_music_bot.music;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.masterzach32.discord_music_bot.App;
import net.masterzach32.discord_music_bot.utils.Constants;
import net.masterzach32.discord_music_bot.utils.JSONReader;

public class PlaylistManager implements JSONReader {
	
	public static final Logger logger = LoggerFactory.getLogger(App.class);
	
	private List<LocalPlaylist> playlists;
	private String guildID;

	public PlaylistManager(String guildID) {
		playlists = new ArrayList<LocalPlaylist>();
		this.guildID = guildID;
	}
	
	public void save() {
		for(LocalPlaylist p : playlists) {
			BufferedWriter fout = null;
			try {
				fout = new BufferedWriter(new FileWriter(Constants.GUILD_SETTINGS + guildID + "/playlists/" + p.getName() + ".json"));
				fout.write(new GsonBuilder().setPrettyPrinting().create().toJson(p));
				fout.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void load() {
		File[] playlists = App.manager.getFile(Constants.GUILD_SETTINGS + guildID + "/playlists/").listFiles();
		for(File file : playlists) {
			RandomAccessFile fin = null;
			byte[] buffer = null;
			
			try {
				fin = new RandomAccessFile(file, "r");
				buffer = new byte[(int) fin.length()];
				fin.readFully(buffer);
				fin.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			String json = new String(buffer);
			this.playlists.add(new Gson().fromJson(json, LocalPlaylist.class));
			logger.debug("loaded:" + file.getName());
		}
	}
	
	public void add(LocalPlaylist p) {
		playlists.add(p);
	}
	
	public void remove(String name) {
		for(LocalPlaylist p : playlists)
			if(p.getName().equals(name))
				playlists.remove(p);
	}
	
	public LocalPlaylist get(String name) {
		for(LocalPlaylist p : playlists)
			if(p.getName().equals(name))
				return p;
		return null;
	}
	
	public String toString() {
		String str = "";
		for(LocalPlaylist p : playlists)
			str += p.getName() + ":" + p.songs() + " ";
		return str;
	}
}