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
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.audio.AudioPlayer;

import javax.sound.sampled.UnsupportedAudioFileException;

public class GuildManager {
	
	private List<GuildSettings> guilds;

	public GuildManager() {
		guilds = new ArrayList<GuildSettings>();
	}
	
	public void loadGuild(IGuild guild) throws IOException, UnirestException, NotStreamableException, UnsupportedAudioFileException, YouTubeDLException, FFMPEGException, MissingPermissionsException {
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
		GuildSettings g = new GuildSettings(guild, temp.getCommandPrefix(), temp.getMaxSkips(), temp.getVolume(), temp.isBotLocked(), temp.isNSFWFilterEnabled(), temp.shouldAnnounce(), temp.shouldChangeNick(), temp.getLastChannel(), temp.getQueue());
		g.getPlaylistManager().load();
		guilds.add(g);

        if(App.client.getVoiceChannelByID(g.getLastChannel()) != null && !g.getLastChannel().equals(""))
            App.client.getVoiceChannelByID(g.getLastChannel()).join();
        List<String> saved = g.getQueue();
        if(saved.size() > 0) {
            AudioSource source;
            for (String url : saved) {
                if (url.contains("youtube"))
                    source = new YouTubeAudio(url);
                else if (url.contains("soundcloud"))
                    source = new SoundCloudAudio(url);
                else
                    source = new AudioStream(url);
                AudioPlayer.getAudioPlayerForGuild(App.client.getGuildByID(guild.getID())).queue(source.getAudioTrack(null, false));
            }
        }
	}

	public void applyGuildSettings() throws MissingPermissionsException, UnirestException, NotStreamableException, UnsupportedAudioFileException, FFMPEGException, YouTubeDLException, IOException {
        for(GuildSettings g : guilds) {
            IGuild guild = App.client.getGuildByID(g.getID());
            App.setVolume(App.guilds.getGuild(guild).getVolume(), guild);

            if(App.client.getVoiceChannelByID(g.getLastChannel()) != null && !g.getLastChannel().equals(""))
                App.client.getVoiceChannelByID(g.getLastChannel()).join();
            List<String> saved = g.getQueue();
            if(saved.size() > 0) {
                AudioSource source;
                for (String url : saved) {
                    if (url.contains("youtube"))
                        source = new YouTubeAudio(url);
                    else if (url.contains("soundcloud"))
                        source = new SoundCloudAudio(url);
                    else
                        source = new AudioStream(url);
                    AudioPlayer.getAudioPlayerForGuild(App.client.getGuildByID(guild.getID())).queue(source.getAudioTrack(null, false));
                }
            }
        }
	}

	public void removeGuild(IGuild guild) {
		for(int i = 0; i < guilds.size(); i++)
			if(guilds.get(i).getID().equals(guild.getID()))
				guilds.remove(guilds.get(i));
	}
	
	public void saveGuildSettings() throws IOException {
		for(int i = 0; i < guilds.size(); i++) {
            GuildSettings guild = guilds.get(i);
			guild.getPlaylistManager().save();

            List<AudioPlayer.Track> tracks = AudioPlayer.getAudioPlayerForGuild(App.client.getGuildByID(guild.getID())).getPlaylist();
            List<String> queue = new ArrayList<>();
            for(AudioPlayer.Track track : tracks)
                if(track != null)
                    queue.add(((AudioTrack) track).getUrl());
            guild.setQueue(queue);

            for(IVoiceChannel c : App.client.getConnectedVoiceChannels())
                if(App.client.getGuildByID(guild.getID()).getVoiceChannelByID(c.getID()) != null)
                    guild.setLastChannel(c.getID());
				else
					guild.setLastChannel("");

            AudioPlayer.getAudioPlayerForGuild(App.client.getGuildByID(guild.getID())).clear();

			BufferedWriter fout = new BufferedWriter(new FileWriter(Constants.GUILD_SETTINGS + guild.getID() + "/" + Constants.GUILD_JSON));
			fout.write(new GsonBuilder().setPrettyPrinting().create().toJson(guild));
			fout.close();
		}
	}
	
	public GuildSettings getGuild(IGuild guild) {
		for(GuildSettings g : guilds)
			if(g != null && g.getID().equals(guild.getID()))
				return g;
		return null;
	}
}