package net.masterzach32.discord_music_bot.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class BotConfig implements JSONReader {
	
	private String discordClientId, discordAuthKey, dbAuthKey, mashapApiKey, se_api_user, se_api_secret;
	private boolean clearCacheOnShutdown;

	/**
	 * Creates a reference to the preferences file, and generates one if it doesn't exist.
	 * @throws IOException 
	 */
	public BotConfig() throws IOException {
		// defaults
		discordClientId = "";
		discordAuthKey = "";
		dbAuthKey = "";
		clearCacheOnShutdown = false;
		mashapApiKey = "";
		se_api_user = "";
		se_api_secret = "";
		
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
		discordClientId = file.discordClientId;
		discordAuthKey = file.getDiscordAuthKey();
		dbAuthKey = file.dbAuthKey;
		clearCacheOnShutdown = file.clearCacheOnShutdown();
		mashapApiKey = file.mashapApiKey;
		se_api_user = file.se_api_user;
		se_api_secret = file.se_api_secret;
	}
	
	public String getDiscordClientId() {
		return discordClientId;
	}
	
	public String getDiscordAuthKey() {
		return discordAuthKey;
	}
	
	public String getDBAuthKey() {
		return dbAuthKey;
	}
	
	public boolean clearCacheOnShutdown() {
		return clearCacheOnShutdown;
	}

	public String getAPIUser() {
		return se_api_user;
	}

	public String getAPISecret() {
		return se_api_secret;
	}
	
	public String getMashapApiKey() {
		return mashapApiKey;
	}
}