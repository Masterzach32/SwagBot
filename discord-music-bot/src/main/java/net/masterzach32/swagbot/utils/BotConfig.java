package net.masterzach32.swagbot.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class BotConfig {

	private String discordClientId, discordAuthKey, dbAuthKey, googleAuthKey, shoutCastApiKey, osuApiKey, scClientId, mashapApiKey, se_api_user, se_api_secret;
	private boolean clearCacheOnShutdown, postBotStats;
	private String[] fightSituations;

	/**
	 * Creates a reference to the preferences file, and generates one if it doesn't exist.
	 * @throws IOException
	 */
	public BotConfig() throws IOException {
		// defaults
		discordClientId = "";
		discordAuthKey = "";
		dbAuthKey = "";
        googleAuthKey = "";
		shoutCastApiKey = "";
		osuApiKey = "";
		scClientId = "";
		clearCacheOnShutdown = false;
		postBotStats = false;
		mashapApiKey = "";
		se_api_user = "";
		se_api_secret = "";
        fightSituations = new String[] {
				"${killed} was defeated by ${killer}!"
		};

		File prefs = new File(Constants.INSTANCE.getBOT_JSON());
		if(!prefs.exists()) {
            prefs.createNewFile();
            save();
		}
	}

	public void save() throws IOException {
		BufferedWriter fout;
		fout = new BufferedWriter(new FileWriter(Constants.INSTANCE.getBOT_JSON()));
		fout.write(new GsonBuilder().setPrettyPrinting().create().toJson(this));
		fout.close();
	}

	public void load() throws IOException {
		RandomAccessFile fin;
		byte[] buffer;

		fin = new RandomAccessFile(Constants.INSTANCE.getBOT_JSON(), "r");
		buffer = new byte[(int) fin.length()];
		fin.readFully(buffer);
		fin.close();

		String json = new String(buffer);
		BotConfig file = new Gson().fromJson(json, BotConfig.class);
		discordClientId = file.discordClientId;
		discordAuthKey = file.getDiscordAuthKey();
		dbAuthKey = file.dbAuthKey;
        googleAuthKey = file.googleAuthKey;
        shoutCastApiKey = file.shoutCastApiKey;
		osuApiKey = file.osuApiKey;
        scClientId = file.scClientId;
		clearCacheOnShutdown = file.clearCacheOnShutdown();
		postBotStats = file.postBotStats;
		mashapApiKey = file.mashapApiKey;
		se_api_user = file.se_api_user;
		se_api_secret = file.se_api_secret;
        fightSituations = file.fightSituations;
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

	public String getGoogleAuthKey() {
        return googleAuthKey;
    }

    public String getShoutCastApiKey() {
        return shoutCastApiKey;
    }

    public String getOsuApiKey() {
		return osuApiKey;
	}

	public String getSCClientId() {
        return scClientId;
    }

	public boolean clearCacheOnShutdown() {
		return clearCacheOnShutdown;
	}

	public boolean shouldPostBotStats() {
		return postBotStats;
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

	public String[] getFightSituations() {
        return fightSituations;
    }
}