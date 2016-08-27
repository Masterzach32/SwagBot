package net.masterzach32.discord_music_bot.commands;

import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public interface CommandEvent {

	public String execute(IMessage message, String[] params) throws RateLimitException, MissingPermissionsException, DiscordException;
	
}