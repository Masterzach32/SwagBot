package net.masterzach32.discord_music_bot;

import java.util.LinkedList;
import java.util.List;

import net.masterzach32.discord_music_bot.music.PlaylistManager;
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
	
	public Guild(IGuild guild, char commandPrefix, int maxSkips, int volume, boolean botLocked, boolean nsfwfilter) {
		this.guild = guild;
		playlists = new PlaylistManager(guild.getID());
		skipIDs = new LinkedList<String>();
		this.guildName = guild.getName();			
		this.commandPrefix = commandPrefix;
		this.maxSkips = maxSkips;
		this.volume = volume;
		this.botLocked = botLocked;
		this.enableNSFWFilter = nsfwfilter;
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
}