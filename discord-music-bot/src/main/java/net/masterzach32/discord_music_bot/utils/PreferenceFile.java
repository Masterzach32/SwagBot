package net.masterzach32.discord_music_bot.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.google.gson.Gson;

public class PreferenceFile {
	
	private String discordAuthKey;
	private int volume;
	private boolean queueEnabled;
	
	private static final String fileName = "prefs.json";

	/**
	 * Creates a reference to the preferences file, and generates one if it doesn't exist.
	 * @throws IOException 
	 */
	public PreferenceFile() throws IOException {
		// defaults
		volume = 50;
		discordAuthKey = "";
		queueEnabled = true;
		
		File prefs = new File(fileName);
		if(!prefs.exists()) {
			prefs.createNewFile();
			save();
		}
	}
	
	public void save() {
		BufferedWriter fout = null;
		try {
			// File optionsFile = new File(path);
			fout = new BufferedWriter(new FileWriter(fileName));
			fout.write(new Gson().toJson(this));
			fout.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void read() {
		RandomAccessFile fin = null;
		byte[] buffer = null;
		
		try {
			// File optionsFile = new File(path);
			fin = new RandomAccessFile(fileName, "r");		// "r" = open file for reading only
			buffer = new byte[(int) fin.length()];
			fin.readFully(buffer);
			fin.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String json = new String(buffer);
		PreferenceFile file = new Gson().fromJson(json, PreferenceFile.class);
		volume = file.getVolume();
		discordAuthKey = file.getDiscordAuthKey();
		queueEnabled = file.isQueueEnabled();
	}
	
	public String getDiscordAuthKey() {
		return discordAuthKey;
	}
	
	public void setVolume(int vol) {
		this.volume = vol;
	}
	
	public int getVolume() {
		return volume;
	}

	public boolean toggleQueueEnabled() {
		return queueEnabled = !queueEnabled;
	}
	
	public boolean isQueueEnabled() {
		return queueEnabled;
	}
}