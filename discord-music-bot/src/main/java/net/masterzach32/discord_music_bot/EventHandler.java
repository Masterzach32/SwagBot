package net.masterzach32.discord_music_bot;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public class EventHandler {

	@EventSubscriber
	public void onReady(EventHandler event) {
		System.out.println("Bot logged into " );
		/*try {
			App.client.changeUsername("Fuck y'all im a java bot");
		} catch (RateLimitException | DiscordException e) {
			e.printStackTrace();
		}*/
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}