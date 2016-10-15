package net.masterzach32.swagbot.music;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import com.mashape.unirest.http.exceptions.UnirestException;
import net.masterzach32.swagbot.music.player.AudioSource;
import net.masterzach32.swagbot.music.player.AudioStream;
import net.masterzach32.swagbot.music.player.SoundCloudAudio;
import net.masterzach32.swagbot.music.player.YouTubeAudio;
import net.masterzach32.swagbot.utils.exceptions.NotStreamableException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.masterzach32.swagbot.App;
import net.masterzach32.swagbot.utils.Constants;

public class PlaylistManager {
	
	public static final Logger logger = LoggerFactory.getLogger(App.class);
	
	private List<LocalPlaylist> playlists;
	private String guildID;

	public PlaylistManager(String guildID) {
		playlists = new ArrayList<>();
		this.guildID = guildID;
	}
	
	public void save() {
		for(LocalPlaylist p : playlists) {
			BufferedWriter fout;
			try {
				fout = new BufferedWriter(new FileWriter(Constants.GUILD_SETTINGS + guildID + "/playlists/" + URLEncoder.encode(p.getName(), "UTF-8") + ".json"));
				fout.write(new GsonBuilder().setPrettyPrinting().create().toJson(p));
				fout.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void load() {
		this.playlists.clear();
		File[] playlists = App.manager.getFile(Constants.GUILD_SETTINGS + guildID + "/playlists/").listFiles();
		for(File file : playlists) {
			RandomAccessFile fin;
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
            JSONObject obj = new JSONObject(json);
            LocalPlaylist p = new LocalPlaylist(obj);
			this.playlists.add(p);
			logger.info("loaded:" + file.getName());
		}
	}
	
	public void add(LocalPlaylist p) {
		playlists.add(p);
	}
	
	public void remove(String name) {
		for(int i = 0; i < playlists.size(); i++)
			if(playlists.get(i).getName().toLowerCase().equals(name.toLowerCase()))
				playlists.remove(i);
	}
	
	public LocalPlaylist get(String name) {
		for(int i = 0; i < playlists.size(); i++)
			if(playlists.get(i).getName().toLowerCase().equals(name.toLowerCase()))
				return playlists.get(i);
		return null;
	}
	
	public String toString() {
		String str = "";
		for(LocalPlaylist p : playlists)
			str += p.getName() + ":" + p.songs() + " ";
		return str;
	}
}