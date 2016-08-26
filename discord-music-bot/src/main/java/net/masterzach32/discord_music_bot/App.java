package net.masterzach32.discord_music_bot;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.sound.sampled.UnsupportedAudioFileException;

import net.masterzach32.discord_music_bot.commands.Command;
import net.masterzach32.discord_music_bot.commands.CommandEvent;
import net.masterzach32.discord_music_bot.music.Playlist;
import net.masterzach32.discord_music_bot.music.PlaylistManager;
import net.masterzach32.discord_music_bot.utils.PreferenceFile;
import sx.blah.discord.api.*;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.*;
import sx.blah.discord.util.audio.AudioPlayer;

public class App {
	
	public static final String installDir = "C:\\Users\\Zach Kozar\\git\\SwagBot\\discord-music-bot\\";
	public static IDiscordClient client;
	public static PreferenceFile prefs;
	public static boolean queueEnabled = true;
	
	public static PlaylistManager playlists;
	
	// 1. jclnm-0ktHM2. ru0K8uYEZWw3. AIiTRr4lxZM4. hdw1uKiTI5c5. 6mqbAnrtWHo6. 6BpcFxNS3i8
	
    public static void main(String[] args) throws DiscordException, IOException {
    	// ((Discord4J.Discord4JLogger) Discord4J.LOGGER).setLevel(Discord4J.Discord4JLogger.Level.TRACE);
    	// https://discordapp.com/oauth2/authorize?client_id=217065780078968833&scope=bot&permissions=8
		prefs = new PreferenceFile();
		prefs.read();
		playlists = new PlaylistManager();
		playlists.load();
    	client = new ClientBuilder().withToken(prefs.getDiscordAuthKey()).build();
    	client.getDispatcher().registerListener(new EventHandler());
    	client.login();
    	
    	// register commands
    	new Command("Help", "help", "Displays a list of all commands and their functions.", 0, new CommandEvent() {
    		public String execute(IMessage message, String[] params) {
    			if(params[0].equals(""))
    				return "Type ~help <command> to get more info on a specific command \n" + Command.listAllCommands();
    			else {
    				for(Command c : Command.commands)
    					if(c.getIdentifier().equals(params[0]))
    						return "Info for ~" + params[0] + "\n" + c.getInfo();
    				return "Could not find command ~" + params[0];
    			}
    		}
    	});
    	new Command("Shutdown Bot", "shutdown", "Logs the bot out of discord and shuts it down. This command doesn't return if the bot succesfully shuts down", 2, new CommandEvent() {
    		public String execute(IMessage message, String[] params){
    			try {
					client.logout();
				} catch (RateLimitException | DiscordException e) {
					e.printStackTrace();
				}
    			prefs.save();
    			playlists.save();
    			System.exit(0);
    		    return "Shutting Down";
    		}
    	});
    	new Command("Shutdown Bot", "stop", "Logs the bot out of discord and shuts it down. This command doesn't return if the bot succesfully shuts down", 2, new CommandEvent() {
    		public String execute(IMessage message, String[] params){
    			try {
					client.logout();
				} catch (RateLimitException | DiscordException e) {
					e.printStackTrace();
				}
    			prefs.save();
    			playlists.save();
    			System.exit(0);
    		    return "Shutting Down";
    		}
    	});
    	new Command("Summon", "summon", "Summons the bot to your voice channel.", 0, new CommandEvent() {
    		public String execute(IMessage message, String[] params) {
    		    IVoiceChannel voicechannel = message.getAuthor().getConnectedVoiceChannels().get(0);
    		    try {
					voicechannel.join();
				} catch (MissingPermissionsException e) {
					e.printStackTrace();
				}
    		    
    		    return "Joined `" + voicechannel.getName() + "`.";
    		}
    	});
    	new Command("Kick", "kick", "Kicks the bot from the current voice channel.", 0, new CommandEvent() {
    		public String execute(IMessage message, String[] params){
    		    IVoiceChannel voicechannel = message.getAuthor().getConnectedVoiceChannels().get(0);
    		    
    		    voicechannel.leave();
    		    
    		    return "Left `" + voicechannel.getName() + "`.";
    		}
    	});
    	new Command("Set Volume", "volume", "Sets the volume of the bot.", 0, new CommandEvent() {
    		public String execute(IMessage message, String[] params){
    		    float vol = Float.parseFloat(params[0]);
    		    setVolume(vol, message.getGuild());
    		    return "Set volume to " + vol/100;
    		}
    	});
    	new Command("Play Cached Files", "cache", "Queue all songs in the cache folder.", 1, new CommandEvent() {
    		public String execute(IMessage message, String[] params) {
    			int i = 0;
    			if(!prefs.isQueueEnabled())
    				return "Music queueing is temporarly disabled";
    		    try {
    		    	File[] files = new File(installDir + "cache\\").listFiles();
    		    	List<File> mp3s = new ArrayList<File>();
    		    	for(File file : files)
    		    		mp3s.add(file);
    		    	Collections.shuffle(mp3s);
    		    	for(i = 0; i < mp3s.size(); i++)
    		    		playAudioFromFile(mp3s.get(i).toString(), message.getGuild());
				} catch (IOException | UnsupportedAudioFileException e) {
					e.printStackTrace();
				}
    		    return "Queued all " + i + " songs in the cache folder.";
    		}
    	});
    	new Command("Clear cache", "clearcache", "Delete all songs in the cache folder.", 2, new CommandEvent() {
    		public String execute(IMessage message, String[] params) {
    			File[] cache = new File("cache/").listFiles();
    			for(File file : cache) {
    				file.delete();
    				System.out.println("Deleted " + file.getName());
    			}
    			System.out.println("Succesfully cleared the cache and deleted " + cache.length + " files.");
    		    return "Deleted " + cache.length + " files.";
    		}
    	});
    	new Command("Playlist", "playlist", "Create, add to, queue, and delete playlists.\nUsage: ~playlist [arg] [name] <param>\nArgs: -create, -add, -remove, -queue, -delete", 1, new CommandEvent() {
    		public String execute(IMessage message, String[] params) {
    			if(params.length < 2)
    				return "Not enough parameters. Type ~help playlist to get help with this command.";
    			String command = params[0], name = params[1];
    			if(command.equals("-create")) {
    				playlists.add(new Playlist(name));
    				return "Created playlist " + name;
    			} else if(command.equals("-add")) {
    				playlists.get(name).add(params[2]);
    				return "Added " + params[2] + " to " + name;
    			} else if(command.equals("-remove")) {
    				playlists.get(name).remove(params[2]);
    				return "Removed " + params[2] + " from " + name;
    			} else if(command.equals("-queue")) {
    				if(!prefs.isQueueEnabled())
        				return "Music queueing is temporarly disabled";
    				playlists.get(name).queue(message.getGuild());
    				return "Queued the playlist " + name;
    			} else if(command.equals("-delete")) {
    				playlists.remove(name);
    				return "Deleted the playlist " + name;
    			} else if(command.equals("-info")) {
    				return "Songs in " + name + ":\n" + playlists.get(name).getInfo();
    			} else if(command.equals("-load")) {
    				playlists.load();
    				return "Re-loaded all playlists";
    			} else if(command.equals("-save")) {
    				playlists.save();
    				return "Saved all playlists";
    			}
		    return "Unrecognized parameters. Type ~help playlist to get help with this command.";
    		}
    	});
    	new Command("Play music", "play", "Add a song to the queue. Usage: ~play [arg] <link>\nOptions: -yt (YouTube), -dl (Direct Link), -f (Local File)", 0, new CommandEvent() {
    		public String execute(IMessage message, String[] params) {
    			boolean s = false;
    			if(!prefs.isQueueEnabled())
    				return "Music queuing is temporarly disabled";
    		    try {
					if(params[0].indexOf('-') != 0)
						s = playAudioFromYouTube(params[0], message.getGuild());
					else if(params[0].equals("-yt"))
						s = playAudioFromYouTube(params[1], message.getGuild());
					else if(params[0].equals("-dl"))
						s = playAudioFromUrl(params[1], message.getGuild());
					else if(params[0].equals("-f"))
						s = playAudioFromFile(params[1], message.getGuild());
					else
						return params[0] + " is not a recognized parameter for ~play.";
				} catch (IOException | UnsupportedAudioFileException e) {
					e.printStackTrace();
				}
    		    if(s)
    		    	return "Queued " + params[params.length-1];
    		    return "An error occured while queueing this file: " + params[params.length-1];
    		}
    	});
    	new Command("Skip", "skip", "Skips the current song in the playlist", 1, new CommandEvent() {
    		public String execute(IMessage message, String[] params){
    			AudioPlayer.getAudioPlayerForGuild(message.getGuild()).skip();
    		    return "Skipped the current song.";
    		}
    	});
    	new Command("Pause", "pause", "Pause the current song", 1, new CommandEvent() {
    		public String execute(IMessage message, String[] params){
    			AudioPlayer.getAudioPlayerForGuild(message.getGuild()).setPaused(true);
    		    return "Paused the playlist.";
    		}
    	});
    	new Command("Resume", "resume", "Resume playback", 1, new CommandEvent() {
    		public String execute(IMessage message, String[] params){
    			AudioPlayer.getAudioPlayerForGuild(message.getGuild()).setPaused(false);
    		    return "Resumed the playlist.";
    		}
    	});
    	new Command("Queue", "queue", "Displays the song queue.", 0, new CommandEvent() {
    		public String execute(IMessage message, String[] params) {
    			AudioPlayer player = AudioPlayer.getAudioPlayerForGuild(message.getGuild());
    			String str = " There are currently " + player.getPlaylistSize() + " song(s) in queue.\n";
    			str += "Currently Playing: " + player.getCurrentTrack() + "(" + player.getCurrentTrack().getCurrentTrackTime() + "/" + player.getCurrentTrack().getTotalTrackTime() + ")\n";
    			for(int i = 1; i < player.getPlaylist().size(); i++)
    				str += i + ". " + player.getPlaylist().get(i) + "(" + player.getPlaylist().get(i).getCurrentTrackTime() + "/" + player.getPlaylist().get(i).getTotalTrackTime() + ")\n";
				return str;
    		}
    	});
    	new Command("Toggle Queue", "t", "Toggles wether songs can be queued.", 0, new CommandEvent() {
    		public String execute(IMessage message, String[] params) {
    			prefs.toggleQueueEnabled();
				return "Queuing songs has been set to " + prefs.isQueueEnabled();
    		}
    	});
    	/*new Command("Hate", "hate", "You know what this does...", 0, new CommandEvent() {
    		public String execute(IMessage message, String[] params) {
				return "command in the works";
    		}
    	});*/
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
        return file.exists();
    }
    
    // Queue audio from specified URL stream for guild
    public static boolean playAudioFromYouTube(String s_url, IGuild guild) throws IOException, UnsupportedAudioFileException {
    	String video_id;
    	if(s_url.indexOf("?v=") < 0)
    		video_id = s_url;
    	else 
    		video_id = s_url.substring(s_url.indexOf("?v=") + 3, s_url.indexOf("=") + 12);
    	if(new File(installDir + "cache\\" + video_id + ".mp3").exists()) {
    		playAudioFromFile(installDir + "cache\\" + video_id + ".mp3", guild);
    		System.out.println("Loading youtube video from cache: " + video_id);
    		return true;
    	}
    	
    	System.out.println("Downloading youtube video: " + video_id);
        ProcessBuilder yt_dn = new ProcessBuilder("py", "youtube-dl", s_url, "--id"/*, "--write-info-json"*/);
        int yt_err = -1;
        File yt = null;
        ProcessBuilder ffmpeg = null;
        int ffmpeg_err = -1;
        
		try {
			yt_err = yt_dn.redirectError(new File(installDir + "logs\\youtube-dl.log")).start().waitFor();
			System.out.println("youtube-dl: " + video_id + " downloaded with exit code " + yt_err);
			for(File file : new File(installDir).listFiles())
	        	if(file.getName().contains(video_id)) {
	        		ffmpeg = new ProcessBuilder("ffmpeg.exe", "-i", file.toString(), installDir + "cache\\" + video_id + ".mp3");
	        		yt = file;
	        	}
			ffmpeg_err = ffmpeg.redirectError(new File(installDir + "logs\\ffmpeg.log")).start().waitFor();
			System.out.println("ffmpeg: " + video_id + " converted and saved with exit code " + ffmpeg_err);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if(yt_err != 0 || ffmpeg_err != 0)
			return false;
		if(yt != null)
			yt.delete();
        
        return playAudioFromFile(installDir + "cache\\" + video_id + ".mp3", guild);
    }
    
    // Change AudioPlayer volume for guild
    public static void setVolume(float vol, IGuild guild) {
        AudioPlayer player = AudioPlayer.getAudioPlayerForGuild(guild);
        player.setVolume(vol/100);
        prefs.setVolume((int) vol); 
    }
}