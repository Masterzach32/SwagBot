package net.masterzach32.discord_music_bot;

import net.masterzach32.discord_music_bot.commands.Command;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.Status;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.audio.events.PauseStateChangeEvent;
import sx.blah.discord.util.audio.events.TrackStartEvent;

public class EventHandler {

	@EventSubscriber
	public void onReady(ReadyEvent event) {
		for(IGuild guild : event.getClient().getGuilds())
			App.setVolume(App.prefs.getVolume(), guild);
		event.getClient().changeStatus(Status.game("Type ~play"));
	}
	
	@EventSubscriber
    public void onMessageEvent(MessageReceivedEvent event) {
		String message = event.getMessage().getContent();
		
		if(true && message.length() < 1 || message.charAt(0) != Command.botprefix)
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
		event.getClient().changeStatus(Status.game(event.getTrack().toString()));
		App.skipCounter = 0;
	}
	
	@EventSubscriber
	public void onPauseStateChangeEvent(PauseStateChangeEvent event) {
		if(event.getNewPauseState())
			event.getClient().changeStatus(Status.game("Paused"));
		else
			event.getClient().changeStatus(Status.game(event.getPlayer().getCurrentTrack().toString()));
	}
}