package net.masterzach32.swagbot;

import java.io.*;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.mashape.unirest.http.exceptions.UnirestException;
import net.masterzach32.swagbot.music.player.*;
import net.masterzach32.swagbot.utils.Constants;
import net.masterzach32.swagbot.utils.exceptions.FFMPEGException;
import net.masterzach32.swagbot.utils.exceptions.NotStreamableException;
import net.masterzach32.swagbot.utils.exceptions.YouTubeDLException;
import sx.blah.discord.handle.impl.obj.Guild;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.RequestBuffer;
import sx.blah.discord.util.audio.AudioPlayer;

import javax.sound.sampled.UnsupportedAudioFileException;

public class GuildManager {
	
	private List<GuildSettings> guilds;

	public GuildManager() {
		guilds = new ArrayList<GuildSettings>();
	}
	
	public GuildSettings loadGuild(IGuild guild) throws IOException, UnirestException, NotStreamableException, UnsupportedAudioFileException, YouTubeDLException, FFMPEGException, MissingPermissionsException {
		App.manager.mkdir(Constants.GUILD_SETTINGS + guild.getID() + "/playlists/");
		File prefs = new File(Constants.GUILD_SETTINGS + guild.getID() + "/" + Constants.GUILD_JSON);
		if(!prefs.exists()) {
			prefs.createNewFile();
			BufferedWriter fout = new BufferedWriter(new FileWriter(Constants.GUILD_SETTINGS + guild.getID() + "/" + Constants.GUILD_JSON));
			fout.write(new GsonBuilder().setPrettyPrinting().create().toJson(new GuildSettings(guild, Constants.DEFAULT_COMMAND_PREFIX, 3, 50, false, false, true, false, null, new ArrayList<>())));
			fout.close();
		}

        RandomAccessFile fin = new RandomAccessFile(Constants.GUILD_SETTINGS + guild.getID() + "/" + Constants.GUILD_JSON, "r"); // "r" = open file for reading only
        byte[] buffer = new byte[(int) fin.length()];
		fin.readFully(buffer);
		fin.close();
		
		String json = new String(buffer);
		GuildSettings temp = new Gson().fromJson(json, GuildSettings.class);
        GuildSettings g;
        if(temp != null)
		    g = new GuildSettings(guild, temp.getCommandPrefix(), temp.getMaxSkips(), temp.getVolume(), temp.isBotLocked(), temp.isNSFWFilterEnabled(), temp.shouldAnnounce(), temp.shouldChangeNick(), temp.getLastChannel(), temp.getQueue());
		else
		    g = new GuildSettings(guild, Constants.DEFAULT_COMMAND_PREFIX, 3, 50, false, false, true, false, null, new ArrayList<>());
        g.getPlaylistManager().load();

        if(!guilds.contains(g))
		    guilds.add(g);

		return g;
	}

	public void applyGuildSettings() throws MissingPermissionsException, UnirestException, NotStreamableException, UnsupportedAudioFileException, FFMPEGException, YouTubeDLException, IOException {
        for(int i = 0; i < guilds.size(); i++) {
			guilds.get(i).applySettings().saveSettings();
        }
	}

	public GuildSettings removeGuild(IGuild guild) {
		for(int i = 0; i < guilds.size(); i++)
			if(guilds.get(i).getID().equals(guild.getID()))
                return guilds.remove(i);
        return null;
	}
	
	public void saveGuildSettings() throws IOException {
		for(int i = 0; i < guilds.size(); i++) {
            guilds.get(i).saveSettings();
		}
	}
	
	public GuildSettings getGuild(IGuild guild) {
		for(int i = 0; i < guilds.size(); i++)
			if(guilds.get(i) != null && guilds.get(i).getID().equals(guild.getID()))
				return guilds.get(i);
		return null;
	}
}