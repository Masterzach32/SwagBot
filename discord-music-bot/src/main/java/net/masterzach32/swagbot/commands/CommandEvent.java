package net.masterzach32.swagbot.commands;

import com.mashape.unirest.http.exceptions.UnirestException;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public interface CommandEvent {

	public void execute(IMessage message, String[] params) throws RateLimitException, MissingPermissionsException, DiscordException, UnirestException;
	
}