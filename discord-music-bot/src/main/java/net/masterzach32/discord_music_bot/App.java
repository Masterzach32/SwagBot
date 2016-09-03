package net.masterzach32.discord_music_bot;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.masterzach32.discord_music_bot.commands.*;
import net.masterzach32.discord_music_bot.music.*;
import net.masterzach32.discord_music_bot.utils.*;
import sx.blah.discord.api.*;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.*;
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
    	
    	manager = new FileManager();
    	prefs = new BotConfig();
    	prefs.load();
    	guilds = new GuildManager();
    	
    	client = new ClientBuilder().withToken(prefs.getDiscordAuthKey()).build();
    	client.getDispatcher().registerListener(new EventHandler());
    	client.login();
    	
    	// register commands
    	new Command("Help", "help", "Displays a list of all commands and their functions.", 0, new CommandEvent() {
    		public String execute(IMessage message, String[] params) {
    			if(params[0].equals(""))
    				return "Type **" + Constants.COMMAND_PREFIX + "help <command>** to get more info on a specific command \n" + Command.listAllCommands();
    			else {
    				for(Command c : Command.commands)
    					if(c.getIdentifier().equals(params[0]))
    						return c.getName() + "**" + Constants.COMMAND_PREFIX + params[0] + "**\n" + c.getInfo();
    				return "Could not find command **" + Constants.COMMAND_PREFIX + params[0] + "**";
    			}
    		}
    	});
    	new Command("Shutdown Bot", "shutdown", "Logs the bot out of discord and shuts it down. This command doesn't return if the bot succesfully shuts down", 2, new CommandEvent() {
    		public String execute(IMessage message, String[] params){
    			try {
					stop();
				} catch (IOException e) {
					e.printStackTrace();
				}
    		    return "Shutting Down";
    		}
    	});
    	new Command("Shutdown Bot", "stop", "Logs the bot out of discord and shuts it down. This command doesn't return if the bot succesfully shuts down", 2, new CommandEvent() {
    		public String execute(IMessage message, String[] params){
    			try {
					stop();
				} catch (IOException e) {
					e.printStackTrace();
				}
    		    return "Shutting Down";
    		}
    	});
    	new Command("Summon", "summon", "Summons the bot to your voice channel.", 0, new CommandEvent() {
    		public String execute(IMessage message, String[] params) {
    			if(guilds.getGuild(message.getGuild()).isBotLocked())
    				return "**SwagBot is currently locked.**";
    			if(message.getAuthor().getConnectedVoiceChannels().size() == 0)
    				return "**You need to be in a voice channel to summon the bot.**";
    		    IVoiceChannel voicechannel = message.getAuthor().getConnectedVoiceChannels().get(0);
    		    try {
					voicechannel.join();
				} catch (MissingPermissionsException e) {
					e.printStackTrace();
				}
    		    
    		    return "Joined **" + voicechannel.getName() + "**.";
    		}
    	});
    	new Command("Kick", "kick", "Kicks the bot from the current voice channel.", 1, new CommandEvent() {
    		public String execute(IMessage message, String[] params){
    			if(guilds.getGuild(message.getGuild()).isBotLocked())
    				return "**SwagBot is currently locked.**";
    		    IVoiceChannel voicechannel = message.getAuthor().getConnectedVoiceChannels().get(0);
    		    
    		    voicechannel.leave();
    		    
    		    return "Left **" + voicechannel.getName() + "**.";
    		}
    	});
    	new Command("Set Volume", "volume", "Sets the volume of the bot.", 0, new CommandEvent() {
    		public String execute(IMessage message, String[] params) {
    			if(guilds.getGuild(message.getGuild()).isBotLocked())
    				return "SwagBot is currently locked.";
    			if(params == null || params[0] == null || params[0] == "")
    				return "Volume is currently set to **" + AudioPlayer.getAudioPlayerForGuild(message.getGuild()).getVolume() * 100+ "**";
    		    float vol = Float.parseFloat(params[0]);
    		    setVolume(vol, message.getGuild());
    		    return "Set volume to **" + vol + "**";
    		}
    	});
    	new Command("Clear cache", "clearcache", "Delete all songs in the cache folder.", 2, new CommandEvent() {
    		public String execute(IMessage message, String[] params) {
    			return "Deleted **" + clearCache() + "** files.";
    		}
    	});
    	new Command("Playlist", "playlist", "Create, add to, queue, and delete playlists.\nUsage: ~playlist [arg] [name] <param>\nArgs: -create, -add, -remove, -queue, -list, -info", 0, new CommandEvent() {
    		public String execute(IMessage message, String[] params) {
    			if(guilds.getGuild(message.getGuild()).isBotLocked())
    				return "**SwagBot is currently locked.**";
    			boolean perms = false;
    			List<IRole> userRoles = message.getAuthor().getRolesForGuild(message.getChannel().getGuild());
    			for(IRole role : userRoles)
    				if(role.getName().equals("Bot Commander"))
    					perms = true;
    			if(params[0].equals("-load") && perms) {
    				guilds.getGuild(message.getGuild()).getPlaylistManager().load();
    				return "**Re-loaded all playlists**";
    			} else if(params[0].equals("-save") && perms) {
    				guilds.getGuild(message.getGuild()).getPlaylistManager().save();
    				return "**Saved all playlists**";
    			} else if(params[0].equals("-list")) {
    				return "**Playlists:** " + guilds.getGuild(message.getGuild()).getPlaylistManager().toString();
    			}
    			if(params.length < 2)
    				return "**Not enough parameters. Type** `~help playlist` **to get help with this command.**";
    			String command = params[0], name = params[1];
    			LocalPlaylist playlist = guilds.getGuild(message.getGuild()).getPlaylistManager().get(name);
    			if(command.equals("-create")) {
    				guilds.getGuild(message.getGuild()).getPlaylistManager().add(new LocalPlaylist(name, false, false));
    				return "Created playlist **" + name + "**";
    			} else if(playlist == null) {
    				return "There is no playlist with the name **" + name + "**";
    			} else if(command.equals("-queue")) {
    				if(!canQueueMusic(message.getAuthor()))
    					return "**You must be in the bot's channel to queue music.**";
    				playlist.queue(message.getAuthor(), message.getChannel(), message.getGuild());
    				return "Queuing the playlist **" + name + "**";
    			} else if(command.equals("-info")) {
    				return "Songs in **" + name + "**:\n" + playlist.getInfo();
    			} else if(command.equals("-lock") && perms) {
    				playlist.toggleLocked();
    				if(playlist.isLocked())
    					return "Playlist **" + name + "** can no longer be edited.";
    				return "Playlist **" + name + "** can now be edited.";
    			} else if(command.equals("-perms") && perms) {
    				playlist.toggleRequiresPerms();
    				if(playlist.requiresPerms())
    					return "Playlist **" + name + "** now requires moderator privelages to edit.";
    				return "Playlist **" + name + "** no longer requires moderator privelages to edit.";
    			} else if(command.equals("-add") && !playlist.requiresPerms() && !playlist.isLocked()) {
    				if(playlist.add(params[2]))
    					return "Added " + params[2] + " to **" + name + "**";
    				return "Playlist **" + name + "** already has " + params[2];
    			} else if(command.equals("-remove") && perms && !playlist.isLocked()) {
    				playlist.remove(params[2]);
    				return "Removed " + params[2] + " from **" + name + "**";
    			} else if(command.equals("-delete") && perms && !playlist.isLocked()) {
    				playlist.remove(name);
    				return "Deleted the playlist **" + name + "**";
    			}
		    return "**You either messed up your parameters or do not have access to this command.**";
    		}
    	});
    	new Command("Play music", "play", "Add a song to the queue. Usage: ~play [arg] <link>\nOptions: -dl (Direct Link), -f (Local File)", 0, new CommandEvent() {
    		public String execute(IMessage message, String[] params) {
    			if(guilds.getGuild(message.getGuild()).isBotLocked())
    				return "**SwagBot is currently locked.**";
    			boolean s = false;
    			if(!canQueueMusic(message.getAuthor()))
    				return "**You must be in the bot's channel to queue music.**";
    		    try {
					if(params[0].indexOf('-') != 0)
						s = playAudioFromYouTube(params[0], true, message.getAuthor(), message.getChannel(), message.getGuild());
					else if(params[0].equals("-dl"))
						s = playAudioFromUrl(params[1], message.getGuild());
					else if(params[0].equals("-f"))
						s = playAudioFromFile(params[1], message.getGuild());
					else
						return params[0] + " is not a recognized parameter for ~play.";
				} catch (IOException | UnsupportedAudioFileException | InterruptedException e) {
					e.printStackTrace();
				}
    		    if(s)
    		    	return "**Queued** " + params[params.length-1];
    		    return "An error occured while queueing this file: " + params[params.length-1];
    		}
    	});
    	new Command("Skip", "skip", "Skips the current song in the playlist.", 0, new CommandEvent() {
    		public String execute(IMessage message, String[] params) {
    			if(guilds.getGuild(message.getGuild()).isBotLocked())
    				return "**SwagBot is currently locked.**";
    			if(guilds.getGuild(message.getGuild()).hasUserSkipped(message.getAuthor().getID()))
    				return "**You already voted to skip this song.**";
    			guilds.getGuild(message.getGuild()).addSkipID(message.getAuthor());
    			if(guilds.getGuild(message.getGuild()).numUntilSkip() == 0 || message.getAuthor().getID().equals("97341976214511616")) {
    				AudioPlayer.getAudioPlayerForGuild(message.getGuild()).skip();
    				guilds.getGuild(message.getGuild()).resetSkipStats();
    				return "**Skipped the current song.**";
    			}
    		    return "**" + guilds.getGuild(message.getGuild()).numUntilSkip() + "** more votes needed to skip the current song.";
    		}
    	});
    	new Command("Shuffle", "shuffle", "Shuffles the queue.", 1, new CommandEvent() {
    		public String execute(IMessage message, String[] params) {
    			if(guilds.getGuild(message.getGuild()).isBotLocked())
    				return "**SwagBot is currently locked.**";
    			AudioPlayer.getAudioPlayerForGuild(message.getGuild()).shuffle();
    		    return "**Shuffled the playlist.**";
    		}
    	});
    	new Command("Pause", "pause", "Pause the current song.", 1, new CommandEvent() {
    		public String execute(IMessage message, String[] params) {
    			if(guilds.getGuild(message.getGuild()).isBotLocked())
    				return "**SwagBot is currently locked.**";
    			AudioPlayer.getAudioPlayerForGuild(message.getGuild()).setPaused(true);
    		    return "**Paused the playlist.**";
    		}
    	});
    	new Command("Resume", "resume", "Resume playback.", 1, new CommandEvent() {
    		public String execute(IMessage message, String[] params) {
    			if(guilds.getGuild(message.getGuild()).isBotLocked())
    				return "**SwagBot is currently locked.**";
    			AudioPlayer.getAudioPlayerForGuild(message.getGuild()).setPaused(false);
    		    return "**Resumed the playlist.**";
    		}
    	});
    	new Command("Clear Queue", "clear", "Clears the queue.", 1, new CommandEvent() {
    		public String execute(IMessage message, String[] params) {
    			if(guilds.getGuild(message.getGuild()).isBotLocked())
    				return "**SwagBot is currently locked.**";
    			AudioPlayer player = AudioPlayer.getAudioPlayerForGuild(message.getGuild());
    			if(player.getPlaylistSize() == 0)
    				return "**No songs to clear.**";
    			player.clear();
    		    return "**Cleared the queue.**";
    		}
    	});
    	new Command("Queue", "queue", "Displays the song queue. WIP", 0, new CommandEvent() {
    		public String execute(IMessage message, String[] params) {
    			AudioPlayer player = AudioPlayer.getAudioPlayerForGuild(message.getGuild());
    			String str = " There are currently **" + (player.getPlaylistSize()-1) + "** song(s) in queue.\n";
    			str += "**Currently Playing: " + ((AudioTrack) player.getPlaylist().get(0)).getTitle() + "**, queued by **" + ((AudioTrack) player.getPlaylist().get(0)).getUser().getName() + "**\n";
    			for(int i = 1; i < player.getPlaylist().size(); i++) {
    				String s = "**(" + i + ")** - **" + ((AudioTrack) player.getPlaylist().get(i)).getTitle() + "**, queued by **" + ((AudioTrack) player.getPlaylist().get(i)).getUser().getName() + "**\n";
    				if((str + s).length() > 1800)
    					break;
    				str += s;
    			
    			}
    			logger.info(str);
    			return str;
    		}
    	});
    	new Command("Lock the bot", "l", "Toggles wether the bot can be used", 0, new CommandEvent() {
    		public String execute(IMessage message, String[] params) {
    			guilds.getGuild(message.getGuild()).toggleBotLocked();
    			if(guilds.getGuild(message.getGuild()).isBotLocked())
    				return "**SwagBot has been locked.**";
    			return "**SwagBot is no longer locked.**";
    		}
    	});
    }
    
    private static void stop() throws IOException {
    	try {
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
    private static boolean playAudioFromFile(String s_file, boolean announce, String id, IUser user, IChannel channel, IGuild guild) throws IOException, UnsupportedAudioFileException {
        File file = new File(s_file); // Get file
        AudioPlayer player = AudioPlayer.getAudioPlayerForGuild(guild); // Get AudioPlayer for guild
        player.queue(new AudioTrack(file, announce, file.getName().substring(0, file.getName().indexOf(id)-1), user, channel)); // Queue file
        logger.info("cached:" + s_file);
        return file.exists();
    }
    
    // Queue audio from specified URL stream for guild
    public static boolean playAudioFromYouTube(String s_url, boolean announce, IUser user, IChannel channel, IGuild guild) throws IOException, UnsupportedAudioFileException, InterruptedException {
    	String video_id;
    	if(s_url.indexOf("?v=") < 0)
    		video_id = s_url;
    	else 
    		video_id = s_url.substring(s_url.indexOf("?v=") + 3, s_url.indexOf("=") + 12);
    	// new code
    	for(File file : manager.getFile(Constants.AUDIO_CACHE).listFiles())
    		if(file.getName().contains(video_id))
        		return playAudioFromFile(Constants.AUDIO_CACHE + file.getName(), announce, video_id, user, channel, guild);
    	
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
    	
    	return playAudioFromFile(Constants.AUDIO_CACHE + yt.getName().substring(0, yt.getName().indexOf(video_id) + 11) + ".mp3", announce, video_id, user, channel, guild);
    }
    
    // Change AudioPlayer volume for guild
    public static void setVolume(float vol, IGuild guild) {
        AudioPlayer player = AudioPlayer.getAudioPlayerForGuild(guild);
        player.setVolume(vol/100);
        guilds.getGuild(guild).setVolume((int) vol); 
    }
}