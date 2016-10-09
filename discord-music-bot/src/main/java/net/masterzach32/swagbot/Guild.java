package net.masterzach32.swagbot;

import java.util.ArrayList;
import java.util.List;

import net.masterzach32.swagbot.music.PlaylistManager;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

public class Guild {
	
	private transient IGuild guild;
	private transient PlaylistManager playlists;
	private transient List<String> skipIDs;
	private String guildName;
	private char commandPrefix;
	private int maxSkips, volume;
	private boolean botLocked, enableNSFWFilter;
	private String lastChannel;
    private List<String> queue;
	
	public Guild(IGuild guild, char commandPrefix, int maxSkips, int volume, boolean botLocked, boolean nsfwfilter, String lastChannel, List<String> queue) {
		this.guild = guild;
		playlists = new PlaylistManager(guild.getID());
		skipIDs = new ArrayList<>();
		this.guildName = guild.getName();			
		this.commandPrefix = commandPrefix;
		this.maxSkips = maxSkips;
		this.volume = volume;
		this.botLocked = botLocked;
		this.enableNSFWFilter = nsfwfilter;
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
}