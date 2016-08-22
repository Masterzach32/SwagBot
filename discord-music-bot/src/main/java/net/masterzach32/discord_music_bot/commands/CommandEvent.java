package net.masterzach32.discord_music_bot.commands;

import sx.blah.discord.handle.obj.IMessage;

public interface CommandEvent {

	public String execute(IMessage message, String[] params);
	
}