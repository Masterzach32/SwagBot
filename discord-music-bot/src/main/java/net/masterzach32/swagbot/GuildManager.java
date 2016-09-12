package net.masterzach32.swagbot;

import java.io.*;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.masterzach32.swagbot.utils.Constants;
import sx.blah.discord.handle.obj.IGuild;

public class GuildManager {
	
	private List<Guild> guilds;

	public GuildManager() {
		guilds = new ArrayList<Guild>();
	}
	
	public void loadGuild(IGuild guild) throws IOException {
		App.manager.mkdir(Constants.GUILD_SETTINGS + guild.getID() + "/playlists/");
		File prefs = new File(Constants.GUILD_SETTINGS + guild.getID() + "/" + Constants.GUILD_JSON);
		if(!prefs.exists()) {
			prefs.createNewFile();
			BufferedWriter fout = null;
			fout = new BufferedWriter(new FileWriter(Constants.GUILD_SETTINGS + guild.getID() + "/" + Constants.GUILD_JSON));
			fout.write(new GsonBuilder().setPrettyPrinting().create().toJson(new Guild(guild, Constants.DEFAULT_COMMAND_PREFIX, 3, 50, false, false)));
			fout.close();
		}
		
		RandomAccessFile fin = null;
		byte[] buffer = null;
		
		fin = new RandomAccessFile(Constants.GUILD_SETTINGS + guild.getID() + "/" + Constants.GUILD_JSON, "r"); // "r" = open file for reading only
		buffer = new byte[(int) fin.length()];
		fin.readFully(buffer);
		fin.close();
		
		String json = new String(buffer);
		Guild temp = new Gson().fromJson(json, Guild.class);
		Guild g = new Guild(guild, temp.getCommandPrefix(), temp.getMaxSkips(), temp.getVolume(), temp.isBotLocked(), temp.isNSFWFilterEnabled());
		g.getPlaylistManager().load();
		guilds.add(g);
		
	}
	
	public void saveGuildSettings() throws IOException {
		for(Guild guild : guilds) {
			guild.getPlaylistManager().save();
			
			BufferedWriter fout = null;
			fout = new BufferedWriter(new FileWriter(Constants.GUILD_SETTINGS + guild.getID() + "/" + Constants.GUILD_JSON));
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