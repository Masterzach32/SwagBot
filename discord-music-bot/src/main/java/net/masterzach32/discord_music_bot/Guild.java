package net.masterzach32.discord_music_bot;

import java.util.LinkedList;
import java.util.List;

import net.masterzach32.discord_music_bot.music.PlaylistManager;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

public class Guild {
	
	protected transient IGuild guild;
	protected transient PlaylistManager playlists;
	protected transient List<String> skipIDs;
	protected int maxSkips, volume;
	
	public Guild(IGuild guild, int maxSkips, int volume) {
		this.guild = guild;
		playlists = new PlaylistManager(guild.getID());
		skipIDs = new LinkedList<String>();
		this.maxSkips = maxSkips;
		this.volume = volume;
	}
	
	public String getID() {
		return guild.getID();
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
	
	public int getVolume() {
		return volume;
	}
	
	public void setVolume(int vol) {
		this.volume = vol;
	}
	
	public PlaylistManager getPlaylistManager() {
		return playlists;
	}
}