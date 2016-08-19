package net.masterzach32.discord_music_bot;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.io.FileUtils;

import sx.blah.discord.api.*;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.*;
import sx.blah.discord.util.audio.AudioPlayer;

public class App {
	
	public static final String 	installDir = "C:\\Users\\Zach Kozar\\git\\SwagBot\\discord-music-bot\\";
	public static IDiscordClient client;
	
    public static void main(String[] args) throws DiscordException, IOException {
    	String loginToken = null;
		loginToken = FileUtils.readFileToString(new File("discord.txt"));
    	client = new ClientBuilder().withToken(loginToken).build();
    	client.getDispatcher().registerListener(new EventHandler());
    	client.login();
    	
    	// TODO: Preferences
    	
    	// register commands
    	new Command("Help", "help", "Displays a list of all commands and their functions.", 0, new CommandEvent() {
    		public String execute(IMessage message, String[] params) {
				return "Here is a list all available commands \n" + Command.listAllCommands();
    		}
    	});
    	new Command("Shutdown Bot", "shutdown", "Logs the bot out of discord and shuts it down. This command doesn't return if the bot succesfully shuts down", 2, new CommandEvent() {
    		public String execute(IMessage message, String[] params){
    			try {
					client.logout();
				} catch (RateLimitException | DiscordException e) {
					e.printStackTrace();
				}
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
    			System.exit(0);
    		    return "Shutting Down";
    		}
    	});
    	new Command("Change Usename", "namechange", "Changes the bot's username.", 1, new CommandEvent() {
    		public String execute(IMessage message, String[] params) {
    			try {
					client.changeUsername(params[0]);
				} catch (RateLimitException | DiscordException e) {
					e.printStackTrace();
				}
				return "Username changed to " + params[0];
    		}
    	});
    	new Command("Summon", "summon", "Summons the bot to your voice channel", 0, new CommandEvent() {
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
    	new Command("Kick", "kick", "Kicks the bot from the current voice channel", 0, new CommandEvent() {
    		public String execute(IMessage message, String[] params){
    		    IVoiceChannel voicechannel = message.getAuthor().getConnectedVoiceChannels().get(0);
    		    
    		    voicechannel.leave();
    		    
    		    return "Left `" + voicechannel.getName() + "`.";
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
    	new Command("Set Volume", "volume", "Sets the volume of the bot", 0, new CommandEvent() {
    		public String execute(IMessage message, String[] params){
    		    float vol = Float.parseFloat(params[0]);
    		    setVolume(vol, message.getGuild());
    		    return "Set volume to " + vol/100;
    		}
    	});
    	new Command("Play from File", "playfile", "Adds a song to the queue.", 0, new CommandEvent() {
    		public String execute(IMessage message, String[] params) {
    			boolean s = false;
    		    try {
					s = playAudioFromFile(params[0], message.getGuild());
				} catch (IOException | UnsupportedAudioFileException e) {
					e.printStackTrace();
				}
    		    if(s)
    		    	return "Queued " + params[0];
    		    return "An error occured while queueing this file: " + params[0];
    		}
    	});
    	new Command("Play Cached Files", "autoplaylist", "Queue all songs in the cache folder", 0, new CommandEvent() {
    		public String execute(IMessage message, String[] params) {
    			int i = 0;
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
    	new Command("Play from URL", "playurl", "Adds a song to the queue.", 0, new CommandEvent() {
    		public String execute(IMessage message, String[] params) {
    			boolean s = false;
    		    try {
					s = playAudioFromUrl(params[0], message.getGuild());
				} catch (IOException | UnsupportedAudioFileException e) {
					e.printStackTrace();
				}
    		    if(s)
    		    	return "Queued " + params[0];
    		    return "An error occured while queueing this url: " + params[0];
    		}
    	});
    	new Command("Play from YouTube", "playyt", "Adds a song to the queue.", 0, new CommandEvent() {
    		public String execute(IMessage message, String[] params) {
    			boolean s = false;
    		    try {
					s = playAudioFromYouTube(params[0], message.getGuild());
				} catch (IOException | UnsupportedAudioFileException e) {
					e.printStackTrace();
				}
    		    if(s)
    		    	return "Queued video " + params[0].substring(params[0].indexOf("=") + 1);
    		    return "An error occured while queueing this youtube video: " + params[0].substring(params[0].indexOf("=") + 1);
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
    private static boolean playAudioFromYouTube(String s_url, IGuild guild) throws IOException, UnsupportedAudioFileException {
    	if(new File(installDir + "cache\\" + s_url.substring(s_url.indexOf("=") + 1) + ".mp3").exists()) {
    		playAudioFromFile(installDir + "cache\\" + s_url.substring(s_url.indexOf("=") + 1) + ".mp3", guild);
    		System.out.println("Loading youtube video from cache: " + s_url.substring(s_url.indexOf("=") + 1));
    		return true;
    	}
    	
    	System.out.println("Downloading youtube video: " + s_url.substring(s_url.indexOf("=") + 1));
        ProcessBuilder yt_dn = new ProcessBuilder("py", "youtube-dl", s_url, "--id"/*, "--write-info-json"*/);
        int yt_err = -1;
        File yt = null;
        ProcessBuilder ffmpeg = null;
        int ffmpeg_err = -1;
        
		try {
			yt_err = yt_dn.redirectError(new File(installDir + "logs\\youtube-dl.log")).start().waitFor();
			System.out.println("youtube-dl: " + s_url.substring(s_url.indexOf("=") + 1) + " downloaded with exit code " + yt_err);
			for(File file : new File(installDir).listFiles())
	        	if(file.getName().contains(s_url.substring(s_url.indexOf("=") + 1))) {
	        		ffmpeg = new ProcessBuilder("ffmpeg.exe", "-i", file.toString(), installDir + "cache\\" + s_url.substring(s_url.indexOf("=") + 1) + ".mp3");
	        		yt = file;
	        	}
			ffmpeg_err = ffmpeg.redirectError(new File(installDir + "logs\\ffmpeg.log")).start().waitFor();
			System.out.println("ffmpeg: " + s_url.substring(s_url.indexOf("=") + 1) + " converted and saved with exit code " + ffmpeg_err);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if(yt_err != 0 || ffmpeg_err != 0)
			return false;
		if(yt != null)
			yt.delete();
        
        return playAudioFromFile(installDir + 	"cache\\" + s_url.substring(s_url.indexOf("=") + 1) + ".mp3", guild);
    }
    
    // Change AudioPlayer volume for guild
    private static void setVolume(float vol, IGuild guild) {
        AudioPlayer player = AudioPlayer.getAudioPlayerForGuild(guild);
        player.setVolume(vol/100);
    }
}