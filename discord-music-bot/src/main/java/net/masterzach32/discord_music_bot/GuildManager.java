package net.masterzach32.discord_music_bot;

import java.io.*;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.masterzach32.discord_music_bot.utils.Constants;
import sx.blah.discord.handle.obj.IGuild;

public class GuildManager {
	
	private List<Guild> guilds;

	public GuildManager() {
		guilds = new ArrayList<Guild>();
	}
	
	public void loadGuild(IGuild guild) throws IOException {
		App.manager.mkdir(Constants.GUILD_SETTINGS + guild.getID() + "/playlists/");
		File prefs = new File(Constants.GUILD_SETTINGS + guild.getID() + "/prefs.json");
		if(!prefs.exists()) {
			prefs.createNewFile();
			BufferedWriter fout = null;
			fout = new BufferedWriter(new FileWriter(Constants.GUILD_SETTINGS + guild.getID() + "/prefs.json"));
			fout.write(new GsonBuilder().setPrettyPrinting().create().toJson(new Guild(guild, 3, 50)));
			fout.close();
		}
		
		RandomAccessFile fin = null;
		byte[] buffer = null;
		
		// File optionsFile = new File(path);
		fin = new RandomAccessFile(Constants.GUILD_SETTINGS + guild.getID() + "/prefs.json", "r"); // "r" = open file for reading only
		buffer = new byte[(int) fin.length()];
		fin.readFully(buffer);
		fin.close();
		
		String json = new String(buffer);
		Guild temp = new Gson().fromJson(json, Guild.class);
		Guild g = new Guild(guild, temp.maxSkips, temp.volume);
		g.playlists.load();
		guilds.add(g);
		
	}
	
	public void saveGuildSettings() throws IOException {
		for(Guild guild : guilds) {
			guild.playlists.save();
			
			BufferedWriter fout = null;
			fout = new BufferedWriter(new FileWriter(Constants.GUILD_SETTINGS + guild.getID() + "/prefs.json"));
			fout.write(new GsonBuilder().setPrettyPrinting().create().toJson(guild));
			fout.close();
		}
	}
	
	public Guild getGuild(IGuild guild) {
		for(Guild g : guilds)
			if(g.getID().equals(guild.getID()))
				return g;
		return null;
	}
}