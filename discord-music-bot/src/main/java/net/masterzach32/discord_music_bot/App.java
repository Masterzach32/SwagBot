package net.masterzach32.discord_music_bot;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.masterzach32.discord_music_bot.api.RandomCat;
import net.masterzach32.discord_music_bot.api.UrbanDefinition;
import net.masterzach32.discord_music_bot.commands.*;
import net.masterzach32.discord_music_bot.music.*;
import net.masterzach32.discord_music_bot.utils.*;
import sx.blah.discord.api.*;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.*;
import sx.blah.discord.util.RequestBuffer.IRequest;
import sx.blah.discord.util.audio.AudioPlayer;

public class App {
	
	public static final Logger logger = LoggerFactory.getLogger(App.class);
	
	public static IDiscordClient client;
	public static BotConfig prefs;
	public static GuildManager guilds;
	public static FileManager manager;
	
    public static void main(String[] args) throws DiscordException, IOException {
    	// https://discordapp.com/oauth2/authorize?client_id=217065780078968833&scope=bot&permissions=8
    	// beta https://discordapp.com/oauth2/authorize?client_id=219554475055120384&scope=bot&permissions=8
    	
    	 Thread.setDefaultUncaughtExceptionHandler(
                 new Thread.UncaughtExceptionHandler() {
                     @Override 
                     public void uncaughtException(Thread t, Throwable e) {
                         System.out.println(t.getName() + ": " + e);
                         e.printStackTrace();
                     }
                 });
    	
    	manager = new FileManager();
    	prefs = new BotConfig();
    	prefs.load();
    	guilds = new GuildManager();
    	
    	client = new ClientBuilder().withToken(prefs.getDiscordAuthKey()).build();
    	client.getDispatcher().registerListener(new EventHandler());
    	client.login();
    	
    	// register commands
    	new Command("Help", "help", "Displays a list of all commands and their functions.", 0, new CommandEvent() {
    		public void execute(IMessage message, String[] params) throws RateLimitException, MissingPermissionsException, DiscordException {
    			if(params[0].equals("")) {
    				Command.listAllCommands(message.getAuthor());
    				sendMessage("A list of commands has been sent to your Direct Messages.", message.getAuthor(), message.getChannel());
    			}
    			else {
    				for(Command c : Command.commands)
    					if(c.getIdentifier().equals(params[0])) {
    						if(message.getChannel().isPrivate())
    							App.client.getOrCreatePMChannel(message.getAuthor()).sendMessage("**" + c.getName() + "** `" + Constants.DEFAULT_COMMAND_PREFIX + c.getIdentifier() + "` Perm Level: " + c.getPermissionLevel() + "\n" + c.getInfo());
    						else
    							sendMessage("**" + c.getName() + "** `" + guilds.getGuild(message.getGuild()).getCommandPrefix() + c.getIdentifier() + "` Perm Level: " + c.getPermissionLevel() + "\n" + c.getInfo(), null, message.getChannel());
    						return;
    					}
    				if(message.getChannel().isPrivate())
    					App.client.getOrCreatePMChannel(message.getAuthor()).sendMessage("Could not find command **" + Constants.DEFAULT_COMMAND_PREFIX + params[0] + "**");
    				else
    					sendMessage("Could not find command **" + guilds.getGuild(message.getGuild()).getCommandPrefix() + params[0] + "**", null, message.getChannel());
    			}
    		}
    	});
    	new Command("Shutdown Bot", "stop", "Logs the bot out of discord and shuts it down. This command doesn't return if the bot succesfully shuts down.", 2, new CommandEvent() {
    		public void execute(IMessage message, String[] params) {
    			try {
					stop();
				} catch (IOException e) {
					e.printStackTrace();
				}
    		}
    	});
    	new Command("Lock the bot", "l", "Toggles wether the bot is locked in this guild.", 1, new CommandEvent() {
    		public void execute(IMessage message, String[] params) {
    			guilds.getGuild(message.getGuild()).toggleBotLocked();
    			if(guilds.getGuild(message.getGuild()).isBotLocked())
    				sendMessage("**SwagBot has been locked.**", message.getAuthor(), message.getChannel());
    			else
    				sendMessage("**SwagBot is no longer locked.**", message.getAuthor(), message.getChannel());
    		}
    	});
    	new Command("Change command prefix", "cp", "Changes the command prefix for the bot in this guild.", 1, new CommandEvent() {
    		public void execute(IMessage message, String[] params) {
    			if(params[0] == "")
    				sendMessage("**Command prefix must be 1 character.**", null, message.getChannel());
    			else {
    				guilds.getGuild(message.getGuild()).setCommandPrefix(params[0].charAt(0));
    				sendMessage("Commamd prefix set to **" + params[0].charAt(0) + "**", message.getAuthor(), message.getChannel());
    			}
    		}
    	});
    	new Command("Ban User", "ban", "Bans the specified user from this guild.", 1, new CommandEvent() {
    		public void execute(IMessage message, String[] params) throws RateLimitException, MissingPermissionsException, DiscordException {
    			String name = "";
    			for(String s : params)
    				name += s;
    				
    			for(IUser user : App.client.getUsers())
    				if(user.getName().equals(name)) {
						message.getGuild().banUser(user);
						sendMessage("@everyone User **" + user + "** has been **banned** from **" + message.getGuild() + "**", null, App.client.getChannelByID("222099708649144320"));
						return;
    				}
    			sendMessage("No user by name **" + name + "** was found in **" + message.getGuild() + "**", null, App.client.getChannelByID("222099708649144320"));
    		}
    	});
    	new Command("Pardon User", "pardon", "Lifts the ban for the specified user from this guild.", 1, new CommandEvent() {
    		public void execute(IMessage message, String[] params) throws RateLimitException, MissingPermissionsException, DiscordException {
    			String name = "";
    			for(String s : params)
    				name += s;
    				
    			for(IUser user : App.client.getUsers())
    				if(user.getName().equals(name)) {
						message.getGuild().pardonUser(user.getID());
						sendMessage("@everyone User **" + user + "** has been **pardoned** from **" + message.getGuild() + "**", null, App.client.getChannelByID("222099708649144320"));
						return;
    				}
    			sendMessage("No user by name **" + name + "** was found in **" + message.getGuild() + "**", null, App.client.getChannelByID("222099708649144320"));
    		}
    	});
    	new Command("Soft Ban User", "softban", "Bans the specified user from this guild, deletes their message history, and then pardons them.", 1, new CommandEvent() {
    		public void execute(IMessage message, String[] params) throws RateLimitException, MissingPermissionsException, DiscordException {
    			String name = "";
    			for(String s : params)
    				name += s;
    				
    			for(IUser user : App.client.getUsers())
    				if(user.getName().equals(name)) {
						message.getGuild().banUser(user, 1);
						message.getGuild().pardonUser(user.getID());
						sendMessage("@everyone User **" + user + "** has been **soft banned** from **" + message.getGuild() + "**", null, App.client.getChannelByID("222099708649144320"));
						return;
    				}
    			sendMessage("No user by name **" + name + "** was found in **" + message.getGuild() + "**", null, App.client.getChannelByID("222099708649144320"));
    		}
    	});
    	new Command("Kick User", "kick", "Kicks the specified user from this guild.", 1, new CommandEvent() {
    		public void execute(IMessage message, String[] params) throws RateLimitException, MissingPermissionsException, DiscordException {
    			String name = "";
    			for(String s : params)
    				name += s;
    				
    			for(IUser user : App.client.getUsers())
    				if(user.getName().equals(name)) {
						message.getGuild().kickUser(user);
						sendMessage("@everyone User **" + user + "** has been **kicked** from **" + message.getGuild() + "**", null, App.client.getChannelByID("222099708649144320"));
						return;
    				}
    			sendMessage("No user by name **" + name + "** was found in **" + message.getGuild() + "**", null, App.client.getChannelByID("222099708649144320"));
    		}
    	});
    	new Command("Prune Messages", "prune", "Deletes the previous X messages", 1, new CommandEvent() {
    		public void execute(IMessage message, String[] params) throws RateLimitException, MissingPermissionsException, DiscordException {
    			IChannel channel = message.getChannel();
    			MessageList list = channel.getMessages();
    			IUser caller = message.getAuthor();
    			if(params[0] == "")
    				sendMessage("Please specify the amount of messages to prune.", null, channel);
    			else {
    				int x = 0;
    				try {
    					x = Integer.parseInt(params[0]);
    				} catch(NumberFormatException e) {
    					sendMessage("Amount must be a number.", null, channel);
    				}
    				if(x < 2 || x > 100)
    					sendMessage("Invalid amount specified. Must prune between 2-100 messages.", null, channel);
    				else {
    					final int toDelete = x;
    					IMessage m = sendMessage("**Removing...**", null, channel);
    					try {
    						logger.info(m + "");
    						RequestBuffer.request(() -> {
    							List<IMessage> deleted;
								try {
									message.delete();
		    						Thread.sleep(500);
		    						deleted = list.deleteFromRange(1, 1 + toDelete);
									for(IMessage d : deleted) {
	    								logger.info("deleted:" + d);
	    							}
								} catch (DiscordException | MissingPermissionsException | InterruptedException e) {
									e.printStackTrace();
								}
    						});
    						logger.info(m + "");
    						m.edit(caller.mention() + " Removed the last " + toDelete + " messages.");
    						Thread.sleep(5000);
    						m.delete();
    					} catch (MissingPermissionsException | DiscordException | InterruptedException e) {
    						e.printStackTrace();
    					}
    				}
    			}
    		}
    	});
    	new Command("Migrate Channels", "migrate", "Move anyone from one channel into another (beta).\nUsage: ~migrate [from] [to]. Use - or _ to replace spaces in Voice Channel names.\nIf no parameters are supplied then the bot will move everyone in the bots channel to the channel you are currently in.", 1, new CommandEvent() {
    		public void execute(IMessage message, String[] params) throws RateLimitException, DiscordException, MissingPermissionsException {
    			if(message.getAuthor().getConnectedVoiceChannels().size() == 0) {
    				sendMessage("**Make sure you are in the channel you want to populate!**", null, message.getChannel());
    				return;
    			}
    			List<IUser> users = null;
    			
    		   	for(String s : params)
    		   		if(s != null) {
    		   			s.replaceAll("_", " ");
    		   			s.replaceAll("-", " ");
    		   		}
    		   	
    		   	IVoiceChannel from = null;
    			for(IVoiceChannel c : client.getConnectedVoiceChannels())
    		    	if(message.getGuild().getVoiceChannelByID(c.getID()) != null) {
    		    		from = c;
    		    		break;
    		    	}
    		   	IVoiceChannel to = message.getAuthor().getConnectedVoiceChannels().get(0);
    		   	
    		   	if(from == null) {
    		   		sendMessage("**Make sure the bot is the channel that you want to migrate from!**.", null, message.getChannel());
    		   		return;
    		   	}
    		   	
    		   	if(params[0] != null && params[0] != "" && params[1] != null && params[1] != "") {
    		   		for(IVoiceChannel c : message.getGuild().getVoiceChannels())
        		   		if(c.getName().equals(params[0])) {
        		   			from = c;
        		   			break;
        		   		}
    		   		for(IVoiceChannel c : message.getGuild().getVoiceChannels())
        		   		if(c.getName().equals(params[1])) {
        		   			to = c;
        		   			break;
        		   		}
    		   	}
    		   	
    		   	users = from.getConnectedUsers();
    		   	moveUsers(users, to);
    		    sendMessage("Sucessfully moved **" + (users.size()-1) + "** guild members from **" + from + "** to **" + to + "**", null, message.getChannel());
    		}
    	});
    	new Command("Summon", "summon", "Summons the bot to your voice channel.", 0, new CommandEvent() {
    		public void execute(IMessage message, String[] params) throws MissingPermissionsException {
    			if(guilds.getGuild(message.getGuild()).isBotLocked()) {
    				sendMessage("**SwagBot is currently locked.**", null, message.getChannel());
    				return;
    			}
    			if(message.getAuthor().getConnectedVoiceChannels().size() == 0) {
    				sendMessage("**You need to be in a voice channel to summon the bot.**", null, message.getChannel());
    				return;
    			}
    			
    		    IVoiceChannel voicechannel = message.getAuthor().getConnectedVoiceChannels().get(0);
				voicechannel.join();
    		    
    		    sendMessage("Joined **" + voicechannel.getName() + "**.", null, message.getChannel());
    		}
    	});
    	new Command("Leave Channel", "leave", "Kicks the bot from the current voice channel.", 1, new CommandEvent() {
    		public void execute(IMessage message, String[] params){
    			if(guilds.getGuild(message.getGuild()).isBotLocked()) {
    				sendMessage("**SwagBot is currently locked.**", null, message.getChannel());
    				return;
    			}
    		    for(IVoiceChannel c : client.getConnectedVoiceChannels())
    		    	if(message.getGuild().getVoiceChannelByID(c.getID()) != null) {
    		    		message.getGuild().getVoiceChannelByID(c.getID()).leave();
    		    		sendMessage("Left **" + message.getGuild().getVoiceChannelByID(c.getID()).getName() + "**.", message.getAuthor(), message.getChannel());
    		    		return;
    		    	}
    		    sendMessage("**The bot is not currently in a voice channel.**", null, message.getChannel());
    		}
    	});
    	new Command("Set Volume", "volume", "Sets the volume of the bot.", 0, new CommandEvent() {
    		public void execute(IMessage message, String[] params) {
    			if(guilds.getGuild(message.getGuild()).isBotLocked()) {
    				sendMessage("**SwagBot is currently locked.**", null, message.getChannel());
    				return;
    			}
    			if(params == null || params[0] == null || params[0] == "")
    				sendMessage("Volume is currently set to **" + AudioPlayer.getAudioPlayerForGuild(message.getGuild()).getVolume() * 100+ "**", null, message.getChannel());
    			else {
    				float vol = Float.parseFloat(params[0]);
    				setVolume(vol, message.getGuild());
        		    sendMessage("Set volume to **" + vol + "**", null, message.getChannel());
    			}
    		}
    	});
    	new Command("Playlist", "playlist", "Create, add to, queue, and delete playlists.\nUsage: ~playlist <action> <playlist> [param]\nActions: -create, -add, -remove, -delete, -queue, -list, -info", 0, new CommandEvent() {
    		public void execute(IMessage message, String[] params) throws RateLimitException, MissingPermissionsException, DiscordException {
    			if(guilds.getGuild(message.getGuild()).isBotLocked()) {
    				sendMessage("**SwagBot is currently locked.**", null, message.getChannel());
    				return;
    			}
    			boolean perms = false;
    			String response = "**You either messed up your parameters or do not have access to this command.**";
    			List<IRole> userRoles = message.getAuthor().getRolesForGuild(message.getChannel().getGuild());
    			for(IRole role : userRoles)
    				if(role.getName().equals("Bot Commander"))
    					perms = true;
    			if(params[0].equals("-load") && perms) {
    				guilds.getGuild(message.getGuild()).getPlaylistManager().load();
    				sendMessage("**Re-loaded all playlists**", null, message.getChannel());
    				return;
    			} else if(params[0].equals("-save") && perms) {
    				guilds.getGuild(message.getGuild()).getPlaylistManager().save();
    				sendMessage("**Saved all playlists**", null, message.getChannel());
    				return;
    			} else if(params[0].equals("-list")) {
    				sendMessage("**Playlists:** " + guilds.getGuild(message.getGuild()).getPlaylistManager().toString(), null, message.getChannel());
    				return;
    			}
    			if(params.length < 2)
    				sendMessage("**Not enough parameters. Type** `" + guilds.getGuild(message.getGuild()).getCommandPrefix() +"help playlist` **to get help with this command.**", null, message.getChannel());
    			String command = params[0], name = params[1];
    			LocalPlaylist playlist = guilds.getGuild(message.getGuild()).getPlaylistManager().get(name);
    			if(command.equals("-create")) {
    				guilds.getGuild(message.getGuild()).getPlaylistManager().add(new LocalPlaylist(name, false, false));
    				response = "Created playlist **" + name + "**";
    			} else if(playlist == null) {
    				response = "There is no playlist with the name **" + name + "**";
    			} else if(command.equals("-queue")) {
    				if(!canQueueMusic(message.getAuthor()))
    					response = "**You must be in the bot's channel to queue music.**";
    				else {
    					playlist.queue(message.getAuthor(), message.getChannel(), message.getGuild());
    					response = "Queuing the playlist **" + name + "**";
    				}
    			} else if(command.equals("-info")) {
    				response = "Songs in **" + name + "**:\n" + playlist.getInfo();
    			} else if(command.equals("-lock") && perms) {
    				playlist.toggleLocked();
    				response = playlist.isLocked() ?  "Playlist **" + name + "** can no longer be edited." : "Playlist **" + name + "** can now be edited.";
    			} else if(command.equals("-perms") && perms) {
    				playlist.toggleRequiresPerms();
    				response = playlist.requiresPerms() ? "Playlist **" + name + "** now requires moderator privelages to edit." : "Playlist **" + name + "** no longer requires moderator privelages to edit.";
    			} else if(command.equals("-add") && !(playlist.requiresPerms() && !perms) && !playlist.isLocked()) {
    				message.delete();
    				response = playlist.add(params[2]) ? "Added " + params[2] + " to **" + name + "**" : "Playlist **" + name + "** already has " + params[2];
    			} else if(command.equals("-remove") && perms && !playlist.isLocked()) {
    				playlist.remove(params[2]);
    				response = "Removed " + params[2] + " from **" + name + "**";
    			} else if(command.equals("-delete") && perms && !playlist.isLocked()) {
    				playlist.remove(name);
    				response = "Deleted the playlist **" + name + "**";
    			}
    			sendMessage(response, null, message.getChannel());
    		}
    	});
    	new Command("Play music", "play", "Add a song to the queue. Usage: ~play [arg] <link>\nOptions: -dl (Direct Link), -f (Local File)", 0, new CommandEvent() {
    		public void execute(IMessage message, String[] params) throws RateLimitException, MissingPermissionsException, DiscordException {
    			if(guilds.getGuild(message.getGuild()).isBotLocked()) {
    				sendMessage("**SwagBot is currently locked.**", null, message.getChannel());
    				return;
    			}
    			boolean s = false;
    			if(!canQueueMusic(message.getAuthor())) {
    				sendMessage("**You must be in the bot's channel to queue music.**", null, message.getChannel());
    				return;
    			}
    		    try {
					if(params[0].indexOf('-') != 0)
						s = playAudioFromYouTube(params[0], true, message.getAuthor(), message.getGuild());
					else if(params[0].equals("-dl"))
						s = playAudioFromUrl(params[1], message.getGuild());
					else if(params[0].equals("-f"))
						s = playAudioFromFile(params[1], message.getGuild());
					else {
						sendMessage(params[0] + " is not a recognized parameter for ~play.", null, message.getChannel());
						return;
					}
				} catch (IOException | UnsupportedAudioFileException | InterruptedException e) {
					e.printStackTrace();
				}
    		    if(s) {
    		    	message.delete();
    		    	waitAndDeleteMessage(sendMessage("**Queued** " + params[params.length-1], null, message.getChannel()), 25);
    		    	
    		    } else
    		    	sendMessage("An error occured while queueing this file: " + params[params.length-1], null, message.getChannel());
    		}
    	});
    	new Command("Skip", "skip", "Skips the current song in the playlist.", 0, new CommandEvent() {
    		public void execute(IMessage message, String[] params) {
    			if(guilds.getGuild(message.getGuild()).hasUserSkipped(message.getAuthor().getID())) {
    				sendMessage("**You already voted to skip this song.**", message.getAuthor(), message.getChannel());
    				return;
    			}
    			guilds.getGuild(message.getGuild()).addSkipID(message.getAuthor());
    			if(guilds.getGuild(message.getGuild()).numUntilSkip() == 0 || message.getAuthor().getID().equals("97341976214511616")) {
    				AudioPlayer.getAudioPlayerForGuild(message.getGuild()).skip();
    				guilds.getGuild(message.getGuild()).resetSkipStats();
    				sendMessage("**Skipped the current song.**", null, message.getChannel());
    			} else 
    				sendMessage("**" + guilds.getGuild(message.getGuild()).numUntilSkip() + "** more votes needed to skip the current song.", null, message.getChannel());
    		}
    	});
    	new Command("Shuffle", "shuffle", "Shuffles the queue.", 1, new CommandEvent() {
    		public void execute(IMessage message, String[] params) {
    			if(guilds.getGuild(message.getGuild()).isBotLocked()) {
    				sendMessage("**SwagBot is currently locked.**", null, message.getChannel());
    				return;
    			}
    			AudioPlayer.getAudioPlayerForGuild(message.getGuild()).shuffle();
    		    sendMessage("**Shuffled the playlist.**", null, message.getChannel());
    		}
    	});
    	new Command("Pause", "pause", "Pause the queue.", 1, new CommandEvent() {
    		public void execute(IMessage message, String[] params) {
    			if(guilds.getGuild(message.getGuild()).isBotLocked()) {
    				sendMessage("**SwagBot is currently locked.**", null, message.getChannel());
    				return;
    			}
    			AudioPlayer.getAudioPlayerForGuild(message.getGuild()).setPaused(true);
    		    sendMessage("**Paused the playlist.**", null, message.getChannel());
    		}
    	});
    	new Command("Resume", "resume", "Resume playback.", 1, new CommandEvent() {
    		public void execute(IMessage message, String[] params) {
    			if(guilds.getGuild(message.getGuild()).isBotLocked()) {
    				sendMessage("**SwagBot is currently locked.**", null, message.getChannel());
    				return;
    			}
    			AudioPlayer.getAudioPlayerForGuild(message.getGuild()).setPaused(false);
    		    sendMessage("**Resumed the playlist.**", null, message.getChannel());
    		}
    	});
    	new Command("Clear Queue", "clear", "Clears the queue.", 1, new CommandEvent() {
    		public void execute(IMessage message, String[] params) {
    			if(guilds.getGuild(message.getGuild()).isBotLocked()) {
    				sendMessage("**SwagBot is currently locked.**", null, message.getChannel());
    				return;
    			}
    			AudioPlayer player = AudioPlayer.getAudioPlayerForGuild(message.getGuild());
    			if(player.getPlaylistSize() == 0)
    				sendMessage("**No songs to clear.**", null, message.getChannel());
    			else {
    				player.clear();
    				sendMessage("**Cleared the queue.**", null, message.getChannel());
    			}
    		}
    	});
    	new Command("Queue", "queue", "Displays the song queue.", 0, new CommandEvent() {
    		public void execute(IMessage message, String[] params) {
    			AudioPlayer player = AudioPlayer.getAudioPlayerForGuild(message.getGuild());
    			String str = " There are currently **" + (player.getPlaylistSize()-1) + "** song(s) in queue.\n";
    			str += "Currently Playing: **" + ((AudioTrack) player.getPlaylist().get(0)).getTitle() + "** (**" + ((AudioTrack) player.getPlaylist().get(0)).getUser().getName() + "**)\n";
    			for(int i = 1; i < player.getPlaylist().size(); i++) {
    				String s = "**(" + i + ")** - " + ((AudioTrack) player.getPlaylist().get(i)).getTitle() + " (**" + ((AudioTrack) player.getPlaylist().get(i)).getUser().getName() + "**)\n";
    				if((str + s).length() > 1800)
    					break;
    				str += s;
    			
    			}
    			logger.info(str);
    			sendMessage(str, null, message.getChannel());
    		}
    	});
    	new Command("Random Cat", "cat", "Posts a random cat picture.", 0, new CommandEvent() {
    		public void execute(IMessage message, String[] params) {
    			sendMessage(new RandomCat().getUrl(), null, message.getChannel());
    		}
    	});
    	new Command("Urban Dictionary Lookup", "urban", "Looks up a term on urban dictionary", 0, new CommandEvent() {
    		public void execute(IMessage message, String[] params) {
    			String term = "";
    			for(String s : params)
    				term += s + " ";
    			UrbanDefinition def = new UrbanDefinition(term);
    			sendMessage("Term Lookup: **" + def.getTerm() + "** " + def.getLink() + "\n```css\nAuthor: " + def.getAuthor() + "\nDefinition: " + def.getDefinition() + "\nExample: " + def.getExample() + "```", null, message.getChannel());
    		}
    	});
    }
    
    private static void stop() throws IOException {
    	logger.info("user initiated shutdown");
    	try {
    		client.changeStatus(Status.game("Shutting Down"));
			client.logout();
		} catch (RateLimitException | DiscordException e) {
			e.printStackTrace();
		}
		prefs.save();
		guilds.saveGuildSettings();
		if(prefs.clearCacheOnShutdown())
			clearCache();
		System.exit(0);
    }
    
    private static int clearCache() {
    	File[] cache = manager.getFile(Constants.AUDIO_CACHE).listFiles();
		int count = 0;
		for(File file : cache) {
			if(file.delete()) {
				logger.info("deleted:" + file.getName());
				count++;
			} else {
				logger.info("failed:" + file.getName());
			}
		}
		if(count < cache.length)
			logger.info("cleared:" + count + "/" + cache.length);
		else
			logger.info("cleared:" + count);
		return count;
    }
    
    private static boolean canQueueMusic(IUser user) {
    	return user.getConnectedVoiceChannels().size() > 0 && client.getConnectedVoiceChannels().contains(user.getConnectedVoiceChannels().get(0));
    }
    
    // Queue audio from specified URL stream for guild
    private static boolean playAudioFromUrl(String s_url, IGuild guild) throws IOException, UnsupportedAudioFileException {
        URL url = new URL(s_url); // Get URL
        AudioPlayer player = AudioPlayer.getAudioPlayerForGuild(guild); // Get AudioPlayer for guild
        player.queue(url); // Queue URL stream
        return true;
    }

    // Queue audio from specified file for guild
    private static boolean playAudioFromFile(String s_file, IGuild guild) throws IOException, UnsupportedAudioFileException {
        File file = new File(s_file); // Get file
        AudioPlayer player = AudioPlayer.getAudioPlayerForGuild(guild); // Get AudioPlayer for guild
        player.queue(file); // Queue file
        logger.info("cached:" + s_file);
        return file.exists();
    }
    
    // Queue audio from specified file for guild
    private static boolean playAudioFromFile(String s_file, boolean announce, String id, IUser user, IGuild guild) throws IOException, UnsupportedAudioFileException {
        File file = new File(s_file); // Get file
        AudioPlayer player = AudioPlayer.getAudioPlayerForGuild(guild); // Get AudioPlayer for guild
        player.queue(new AudioTrack(file, announce, file.getName().substring(0, file.getName().indexOf(id)-1), user)); // Queue file
        logger.info("cached:" + s_file);
        return file.exists();
    }
    
    // Queue audio from specified URL stream for guild
    public static boolean playAudioFromYouTube(String s_url, boolean announce, IUser user, IGuild guild) throws IOException, UnsupportedAudioFileException, InterruptedException {
    	String video_id;
    	if(s_url.indexOf("?v=") < 0)
    		video_id = s_url;
    	else
    		video_id = s_url.substring(s_url.indexOf("?v=") + 3, s_url.indexOf("=") + 12);
    	// new code
    	for(File file : manager.getFile(Constants.AUDIO_CACHE).listFiles())
    		if(file.getName().contains(video_id))
        		return playAudioFromFile(Constants.AUDIO_CACHE + file.getName(), announce, video_id, user, guild);
    	
    	logger.info("downloading:" + video_id);
    	ProcessBuilder yt_dn = new ProcessBuilder("py", Constants.BINARY_STORAGE + "youtube-dl", s_url);
    	int yt_err = -1;
    	
    	yt_err = yt_dn.redirectError(new File(Constants.LOG_STORAGE + "youtube-dl.log")).start().waitFor();
		logger.info("youtube-dl:" + video_id + " exit:" + yt_err);
		
		File yt = null;
    	ProcessBuilder ffmpeg = null;
    	int ffmpeg_err = -1;
		for(File file : manager.getFile(Constants.WORKING_DIRECTORY).listFiles())
			if(file.getName().contains(video_id)) {
				ffmpeg = new ProcessBuilder(Constants.BINARY_STORAGE + "ffmpeg.exe", "-i", file.toString(), Constants.AUDIO_CACHE + file.getName().substring(0, file.getName().indexOf(video_id) + 11) + ".mp3");
				yt = file;
			}
		ffmpeg_err = ffmpeg.redirectError(new File(Constants.LOG_STORAGE + "ffmpeg.log")).start().waitFor();
		logger.info("ffmpeg:" + video_id + " exit:" + ffmpeg_err);
		
    	if(yt_err != 0 || ffmpeg_err != 0) {
    		logger.info("failed:" + s_url);
    		return false;
    	} 
    	if(yt != null)
    		yt.delete();
    	
    	return playAudioFromFile(Constants.AUDIO_CACHE + yt.getName().substring(0, yt.getName().indexOf(video_id) + 11) + ".mp3", announce, video_id, user, guild);
    }
    
    // Change AudioPlayer volume for guild
    public static void setVolume(float vol, IGuild guild) {
        AudioPlayer player = AudioPlayer.getAudioPlayerForGuild(guild);
        player.setVolume(vol/100);
        guilds.getGuild(guild).setVolume((int) vol); 
    }
    
    public static IMessage sendMessage(String message, IUser user, IChannel channel) {
    	return RequestBuffer.request(() -> {
    		try {
    			if(user != null)
    				return new MessageBuilder(client).withContent(user.mention() + " " + message).withChannel(channel).build();
    			return new MessageBuilder(client).withContent(message).withChannel(channel).build();
			} catch (DiscordException | MissingPermissionsException e) {
				e.printStackTrace();
			}
			return null;
    	}).get();
    }
    
    private static void waitAndDeleteMessage(IMessage message, int seconds) {
		RequestBuffer.request(() -> {
		   	try {
		   		Thread.sleep(seconds * 1000);
				message.delete();
			} catch (MissingPermissionsException | DiscordException | InterruptedException e) {
				e.printStackTrace();
			}
		});
    }
    
    private static void moveUsers(List<IUser> users, IVoiceChannel to) {
    	RequestBuffer.request(() -> {
	   		for(IUser user : users) {
		   		try {
					user.moveToVoiceChannel(to);
				} catch (DiscordException | MissingPermissionsException e) {
					e.printStackTrace();
				}
		   	}
		});
    }
}