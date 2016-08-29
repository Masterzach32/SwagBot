package net.masterzach32.discord_music_bot.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.google.gson.Gson;

public class BotConfig implements JSONReader {
	
	private String discordAuthKey;
	private int volume, skipCounter;
	private boolean queueEnabled, clearCacheOnShutdown;

	/**
	 * Creates a reference to the preferences file, and generates one if it doesn't exist.
	 * @throws IOException 
	 */
	public BotConfig() throws IOException {
		// defaults
		volume = 50;
		skipCounter = 3;
		discordAuthKey = "";
		queueEnabled = true;
		clearCacheOnShutdown = false;
		
		File prefs = new File(Constants.BOT_SETTINGS);
		if(!prefs.exists()) {
			prefs.createNewFile();
			save();
		}
	}
	
	public void save() {
		BufferedWriter fout = null;
		try {
			fout = new BufferedWriter(new FileWriter(Constants.BOT_SETTINGS));
			fout.write(new Gson().toJson(this));
			fout.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void load() {
		RandomAccessFile fin = null;
		byte[] buffer = null;
		
		try {
			// File optionsFile = new File(path);
			fin = new RandomAccessFile(Constants.BOT_SETTINGS, "r");		// "r" = open file for reading only
			buffer = new byte[(int) fin.length()];
			fin.readFully(buffer);
			fin.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String json = new String(buffer);
		BotConfig file = new Gson().fromJson(json, BotConfig.class);
		volume = file.getVolume();
		skipCounter = file.getSkipCounter();
		discordAuthKey = file.getDiscordAuthKey();
		queueEnabled = file.isQueueEnabled();
		clearCacheOnShutdown = file.clearCacheOnShutdown();
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
	
	public boolean clearCacheOnShutdown() {
		return clearCacheOnShutdown;
	}
	
	public int getSkipCounter() {
		return skipCounter;
	}
}