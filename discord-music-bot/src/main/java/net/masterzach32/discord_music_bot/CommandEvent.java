package net.masterzach32.discord_music_bot;

import sx.blah.discord.handle.obj.IMessage;

public interface CommandEvent {

	public String execute(IMessage message, String[] params);
	
}