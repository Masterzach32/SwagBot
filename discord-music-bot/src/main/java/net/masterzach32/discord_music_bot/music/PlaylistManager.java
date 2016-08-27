package net.masterzach32.discord_music_bot.music;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

public class PlaylistManager {
	
	private List<LocalPlaylist> playlists;

	public PlaylistManager() {
		playlists = new ArrayList<LocalPlaylist>();
	}
	
	public void load() {
		File pfolder = new File("playlists/");
		pfolder.mkdirs();
		File[] playlists = pfolder.listFiles();
		for(File file : playlists) {
			RandomAccessFile fin = null;
			byte[] buffer = null;
			
			try {
				// File optionsFile = new File(path);
				fin = new RandomAccessFile(file, "r");		// "r" = open file for reading only
				buffer = new byte[(int) fin.length()];
				fin.readFully(buffer);
				fin.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			String json = new String(buffer);
			this.playlists.add(new Gson().fromJson(json, LocalPlaylist.class));
			System.out.println("Loaded playlist " + file.getName());
		}
	}
	
	public void save() {
		for(LocalPlaylist p : playlists) {
			BufferedWriter fout = null;
			try {
				// File optionsFile = new File(path);
				fout = new BufferedWriter(new FileWriter("playlists/" + p.getName() + ".json"));
				fout.write(new Gson().toJson(p));
				fout.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
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