package net.masterzach32.discord_music_bot.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class BotConfig implements JSONReader {
	
	private String discordAuthKey;
	private boolean clearCacheOnShutdown;

	/**
	 * Creates a reference to the preferences file, and generates one if it doesn't exist.
	 * @throws IOException 
	 */
	public BotConfig() throws IOException {
		// defaults
		discordAuthKey = "";
		clearCacheOnShutdown = false;
		
		File prefs = new File(Constants.BOT_JSON);
		if(!prefs.exists()) {
			prefs.createNewFile();
			save();
		}
	}
	
	public void save() throws IOException {
		BufferedWriter fout = null;
		fout = new BufferedWriter(new FileWriter(Constants.BOT_JSON));
		fout.write(new GsonBuilder().setPrettyPrinting().create().toJson(this));
		fout.close();
	}
	
	public void load() throws IOException {
		RandomAccessFile fin = null;
		byte[] buffer = null;
		
		fin = new RandomAccessFile(Constants.BOT_JSON, "r");
		buffer = new byte[(int) fin.length()];
		fin.readFully(buffer);
		fin.close();
		
		String json = new String(buffer);
		BotConfig file = new Gson().fromJson(json, BotConfig.class);
		discordAuthKey = file.getDiscordAuthKey();
		clearCacheOnShutdown = file.clearCacheOnShutdown();
	}
	
	public String getDiscordAuthKey() {
		return discordAuthKey;
	}
	
	public boolean clearCacheOnShutdown() {
		return clearCacheOnShutdown;
	}
}