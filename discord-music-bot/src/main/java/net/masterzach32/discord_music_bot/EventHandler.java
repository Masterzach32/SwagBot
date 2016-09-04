package net.masterzach32.discord_music_bot;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.masterzach32.discord_music_bot.commands.Command;
import net.masterzach32.discord_music_bot.music.AudioTrack;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.*;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.*;
import sx.blah.discord.util.audio.events.*;
public class EventHandler {
	
	public static final Logger logger = LoggerFactory.getLogger(EventHandler.class);
	
	@EventSubscriber
	public void onGuildCreateEvent(GuildCreateEvent event) {
		IGuild guild = event.getGuild();
        try {
			App.guilds.loadGuild(guild);
		} catch (IOException e) {
			e.printStackTrace();
		}
        
	}

	@EventSubscriber
	public void onReady(ReadyEvent event) {
		for(IGuild guild : event.getClient().getGuilds())
			App.setVolume(App.guilds.getGuild(guild).getVolume(), guild);
		event.getClient().changeStatus(Status.game("Queue some music!"));
	}
	
	@EventSubscriber
    public void onMessageEvent(MessageReceivedEvent event) {
		String message = event.getMessage().getContent();
		
		if(event.getMessage().getGuild() == null)
			return;
		
		if(true && message.length() < 1 || message.charAt(0) != App.guilds.getGuild(event.getMessage().getGuild()).getCommandPrefix())
			return;
		
		String identifier;
		String[] params = {""};
		if(message.indexOf(' ') > 0) {
			identifier = message.substring(1, message.indexOf(' '));
			params = message.substring(message.indexOf(' ') + 1).split(" ");
		} else { 
			identifier = message.substring(1);
		}
		
		try {
			event.getMessage().reply(Command.executeCommand(event.getMessage(), identifier, params));
		} catch (RateLimitException | DiscordException | MissingPermissionsException e) {
			e.printStackTrace();
		}
    }
	
	@EventSubscriber
	public void onTrackStartEvent(TrackStartEvent event) {
		event.getClient().changeStatus(Status.game(((AudioTrack) event.getPlayer().getCurrentTrack()).getTitle()));
		logger.info("playing:" + Status.game(((AudioTrack) event.getPlayer().getCurrentTrack()).getTitle()));
		try {
			if(((AudioTrack) event.getPlayer().getCurrentTrack()).shouldAnnounce())
				new MessageBuilder(App.client).withContent(((AudioTrack) event.getPlayer().getCurrentTrack()).getUser().mention() + ", Your song, **" + ((AudioTrack) event.getPlayer().getCurrentTrack()).getTitle() + "** is now playing in **" + event.getPlayer().getGuild().getName() + "!**").withChannel(((AudioTrack) event.getPlayer().getCurrentTrack()).getChannel()).build();
		} catch (RateLimitException | DiscordException | MissingPermissionsException e) {
			e.printStackTrace();
		}
		App.guilds.getGuild(event.getPlayer().getGuild()).resetSkipStats();
	}
	
	@EventSubscriber
	public void onTrackFinishEvent(TrackFinishEvent event) {
		if(event.getPlayer().getPlaylistSize() == 0)
			event.getClient().changeStatus(Status.game("Queue some music!"));
	}
	
	@EventSubscriber
	public void onPauseStateChangeEvent(PauseStateChangeEvent event) {
		if(event.getNewPauseState())
			event.getClient().changeStatus(Status.game("Paused"));
		else
			event.getClient().changeStatus(Status.game(((AudioTrack) event.getPlayer().getCurrentTrack()).getTitle()));
	}
}