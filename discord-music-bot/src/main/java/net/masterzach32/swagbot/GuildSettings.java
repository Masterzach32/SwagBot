package net.masterzach32.swagbot;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.GsonBuilder;
import net.masterzach32.swagbot.music.PlaylistManager;
import net.masterzach32.swagbot.music.player.AudioTrack;
import net.masterzach32.swagbot.utils.Constants;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.audio.AudioPlayer;

public class GuildSettings {
	
	private transient IGuild guild;
	private transient PlaylistManager playlists;
	private transient List<String> skipIDs;
	private String guildName;
	private char commandPrefix;
	private int maxSkips, volume;
	private boolean botLocked, enableNSFWFilter, announce, changeNick;
	private String lastChannel;
    private List<String> queue;
	
	public GuildSettings(IGuild guild, char commandPrefix, int maxSkips, int volume, boolean botLocked, boolean nsfwfilter, boolean announce, boolean changeNick, String lastChannel, List<String> queue) {
		this.guild = guild;
		playlists = new PlaylistManager(guild.getID());
		skipIDs = new ArrayList<>();
		this.guildName = guild.getName();			
		this.commandPrefix = commandPrefix;
		this.maxSkips = maxSkips;
		this.volume = volume;
		this.botLocked = botLocked;
		this.enableNSFWFilter = nsfwfilter;
		this.announce = announce;
        this.changeNick = changeNick;
        this.lastChannel = lastChannel;
        this.queue = queue;
	}
	
	public String getID() {
		return guild.getID();
	}
	
	public String getName() {
		return guildName;
	}
	
	public void resetSkipStats() {
		skipIDs.clear();
	}
	
	public void addSkipID(IUser user) {
		skipIDs.add(user.getID());
	}
	
	public boolean hasUserSkipped(String userID) {
		return skipIDs.contains(userID);
	}
	
	public int numUntilSkip() {
		return maxSkips - skipIDs.size();
	}
	
	public int getMaxSkips() {
		return maxSkips;
	}

	public void setMaxSkips(int maxSkips) {
		this.maxSkips = maxSkips;
	}
	
	public int getVolume() {
		return volume;
	}
	
	public void setVolume(int vol) {
		this.volume = vol;
	}
	
	public boolean toggleBotLocked() {
		return botLocked = !botLocked;
	}
	
	public boolean isBotLocked() {
		return botLocked;
	}
	
	public PlaylistManager getPlaylistManager() {
		return playlists;
	}
	
	public char getCommandPrefix() {
		return commandPrefix;
	}
	
	public void setCommandPrefix(char prefix) {
		this.commandPrefix = prefix;
	}
	
	public boolean isNSFWFilterEnabled() {
		return enableNSFWFilter;
	}
	
	public void toggleNSFWFilter() {
		enableNSFWFilter = !enableNSFWFilter;
	}

    public boolean shouldAnnounce() {
        return announce;
    }

    public void setShouldAnnounce(boolean announce) {
        this.announce = announce;
    }

    public boolean shouldChangeNick() {
        return changeNick;
    }

    public void setChangeNick(boolean changeNick) {
        this.changeNick = changeNick;
    }

	public String getLastChannel() {
        return lastChannel;
    }

    public void setLastChannel(String id) {
        lastChannel = id;
    }

    public void setQueue(List<String> queue) {
        this.queue = queue;
    }

    public List<String> getQueue() {
        return queue;
    }

    public void saveSettings() throws IOException {
        getPlaylistManager().save();

        List<AudioPlayer.Track> tracks = AudioPlayer.getAudioPlayerForGuild(App.client.getGuildByID(guild.getID())).getPlaylist();
        List<String> queue = new ArrayList<>();
        for(AudioPlayer.Track track : tracks)
            if(track != null)
                queue.add(((AudioTrack) track).getUrl());
        setQueue(queue);

        AudioPlayer.getAudioPlayerForGuild(App.client.getGuildByID(guild.getID())).clear();

        BufferedWriter fout = new BufferedWriter(new FileWriter(Constants.GUILD_SETTINGS + guild.getID() + "/" + Constants.GUILD_JSON));
        fout.write(new GsonBuilder().setPrettyPrinting().create().toJson(guild));
        fout.close();
    }

    public AudioPlayer getAudioPlayer() {
        return AudioPlayer.getAudioPlayerForGuild(guild);
    }
}