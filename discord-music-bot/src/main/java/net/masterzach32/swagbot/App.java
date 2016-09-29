package net.masterzach32.swagbot;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.sound.sampled.UnsupportedAudioFileException;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.sun.org.apache.bcel.internal.generic.IUSHR;
import net.masterzach32.swagbot.utils.exceptions.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mashape.unirest.http.Unirest;

import net.masterzach32.swagbot.api.*;
import net.masterzach32.swagbot.api.jokes.*;
import net.masterzach32.swagbot.commands.*;
import net.masterzach32.swagbot.music.*;
import net.masterzach32.swagbot.utils.*;
import sx.blah.discord.Discord4J;
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

    public static void main(String[] args) throws DiscordException, IOException, UnirestException {
        // https://discordapp.com/oauth2/authorize?client_id=217065780078968833&scope=bot&permissions=8
        // beta https://discordapp.com/oauth2/authorize?client_id=219554475055120384&scope=bot&permissions=8
        if (Discord4J.LOGGER instanceof Discord4J.Discord4JLogger) {
            ((Discord4J.Discord4JLogger) Discord4J.LOGGER).setLevel(Discord4J.Discord4JLogger.Level.INFO);
        }

        // load all files into bot
        manager = new FileManager();
        // load bot settings
        prefs = new BotConfig();
        prefs.load();
        // load guild-specific settings
        guilds = new GuildManager();

        client = new ClientBuilder().withToken(prefs.getDiscordAuthKey()).build();
        client.getDispatcher().registerListener(new EventHandler());
        client.login(false);

        // register commands
        new Command("Help", "help", "Displays a list of all commands and their functions.", 0, (message, params) -> {
            if (params.length == 0) {
                Command.listAllCommands(message.getAuthor());
                if (!message.getChannel().isPrivate())
                    sendMessage("A list of commands has been sent to your Direct Messages.", message.getAuthor(), message.getChannel());
            } else {
                for (Command c : Command.commands)
                    if (c.getIdentifier().equals(params[0])) {
                        if (message.getChannel().isPrivate())
                            App.client.getOrCreatePMChannel(message.getAuthor()).sendMessage("**" + c.getName() + "** `" + Constants.DEFAULT_COMMAND_PREFIX + c.getIdentifier() + "` Perm Level: " + c.getPermissionLevel() + "\n" + c.getInfo());
                        else
                            sendMessage("**" + c.getName() + "** `" + guilds.getGuild(message.getGuild()).getCommandPrefix() + c.getIdentifier() + "` Perm Level: " + c.getPermissionLevel() + "\n" + c.getInfo(), null, message.getChannel());
                        return;
                    }
                if (message.getChannel().isPrivate())
                    App.client.getOrCreatePMChannel(message.getAuthor()).sendMessage("Could not find command **" + Constants.DEFAULT_COMMAND_PREFIX + params[0] + "**");
                else
                    sendMessage("Could not find command **" + guilds.getGuild(message.getGuild()).getCommandPrefix() + params[0] + "**", null, message.getChannel());
            }
        });
        new Command("Shutdown Bot", "stop", "Logs the bot out of discord and shuts it down. This command doesn't return if the bot successfully shuts down.", 2, (message, params) -> {
            stop(true);
        });
        new Command("Restart Bot", "restart", "Calls stop and restarts the bot.", 2, (message, params) -> {
            try {
                restart();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        new Command("Update Bot", "update", "Downloads an update for the bot, if there is one.", 2, (message, params) -> {
            update();
        });
        new Command("Reload", "reload", "Reloads bot settings", 2, ((message, params) -> {
            prefs.load();
        }));
        new Command("Lock the bot", "l", "Toggles wether the bot is locked in this guild.", 1, (message, params) -> {
            guilds.getGuild(message.getGuild()).toggleBotLocked();
            if (guilds.getGuild(message.getGuild()).isBotLocked())
                sendMessage("**SwagBot has been locked.**", null, message.getChannel());
            else
                sendMessage("**SwagBot is no longer locked.**", null, message.getChannel());
        });
        new Command("Toggle NSFW Filter", "nsfw", "Toggles whether the bot filters out images that may be considered nsfw content.", 1, (message, params) -> {
            guilds.getGuild(message.getGuild()).toggleNSFWFilter();;
            if(guilds.getGuild(message.getGuild()).isNSFWFilterEnabled())
                sendMessage("**NSFW Filter enabled.**", null, message.getChannel());
            else
                sendMessage("**NSFW Filter disabled.**", null, message.getChannel());
        });
        new Command("Change command prefix", "cp", "Changes the command prefix for the bot in this guild.", 1, (message, params) -> {
            if (params.length == 0 || params[0].length() > 1)
                sendMessage("**Command prefix must be 1 character.**", null, message.getChannel());
            else {
                guilds.getGuild(message.getGuild()).setCommandPrefix(params[0].charAt(0));
                sendMessage("Command prefix set to **" + params[0].charAt(0) + "**", message.getAuthor(), message.getChannel());
            }
        });
        new Command("Change Status", "status", "Change the bots status", 2, (message, params) -> {
            if(params.length > 0) {
                String status = "";
                for(String str : params)
                    status += str + " ";
                client.changeStatus(Status.game(status.substring(0, status.length()-1)));
            }
        });
        new Command("Ban User", "ban", "Bans the specified user(s) from this guild.", 1, (message, params) -> {
            for (IUser user : message.getMentions()) {
                message.getGuild().banUser(user);
                sendMessage("@everyone User **" + user + "** has been **banned** from **" + message.getGuild() + "**", null, message.getGuild().getChannelByID(message.getGuild().getID()));
            }
        });
        new Command("Pardon User", "pardon", "Lifts the ban for the specified user(s) from this guild.", 1, (message, params) -> {
            for (IUser user : message.getMentions()) {
                message.getGuild().pardonUser(user.getID());
                sendMessage("@everyone User **" + user + "** has been **pardoned** from **" + message.getGuild() + "**", null, message.getGuild().getChannelByID(message.getGuild().getID()));
            }
        });
        new Command("Soft Ban User", "softban", "Bans the specified user from this guild, deletes their message history, and then pardons them.", 1, (message, params) -> {
            for (IUser user : message.getMentions()) {
                message.getGuild().banUser(user, 1);
                message.getGuild().pardonUser(user.getID());
                sendMessage("@everyone User **" + user + "** has been **soft banned** from **" + message.getGuild() + "**", null, message.getGuild().getChannelByID(message.getGuild().getID()));
            }
        });
        new Command("Kick User", "kick", "Kicks the specified user from this guild.", 1, (message, params) -> {
            for (IUser user : message.getMentions()) {
                message.getGuild().kickUser(user);
                sendMessage("@everyone User **" + user + "** has been **kicked** from **" + message.getGuild() + "**", null, message.getGuild().getChannelByID(message.getGuild().getID()));
            }
        });
        new Command("Prune Messages", "prune", "Deletes the previous X messages", 1, (message, params) -> {
            IChannel channel = message.getChannel();
            IUser caller = message.getAuthor();
            if (params.length == 0)
                sendMessage("Please specify the amount of messages to prune.", null, channel);
            else {
                int x = 0;
                try {
                    x = Integer.parseInt(params[0]);
                } catch (NumberFormatException e) {
                    sendMessage("Amount must be a number.", null, channel);
                }
                if (x < 2 || x > 100)
                    sendMessage("Invalid amount specified. Must prune between 2-100 messages.", null, channel);
                else {
                    final int toDelete = x;
                    IMessage m = sendMessage("**Fetching messages...**", null, channel);
                    try {
                        logger.info(m + "");
                        RequestBuffer.request(() -> {
                            MessageList list = channel.getMessages();
                            List<IMessage> deleted;
                            try {
                                message.delete();
                                Thread.sleep(500);
                                deleted = list.deleteFromRange(1, 1 + toDelete);
                                for (IMessage d : deleted) {
                                    logger.info("deleted:" + d);
                                }
                            } catch (DiscordException | MissingPermissionsException | InterruptedException e) {
                                e.printStackTrace();
                            }
                        });
                        logger.info(m + "");
                        m.edit(caller.mention() + " Removed the last " + toDelete + " messages.");
                        Thread.sleep(4000);
                        m.delete();
                    } catch (MissingPermissionsException | DiscordException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        new Command("Migrate Channels", "migrate", "Move anyone from one channel into another (beta).\nUsage: ~migrate [from] [to]. Use - or _ to replace spaces in Voice Channel names. \nIf no parameters are supplied then the bot will move everyone in the bots channel to the channel you are currently in.", 1, (message, params) -> {
            IVoiceChannel from = null, to = null;
            if (params.length == 2) {
                for (int i = 0; i < params.length; i++)
                    if (params[i] != null) {
                        params[i] = params[i].replaceAll("_", " ");
                        params[i] = params[i].replaceAll("-", " ");
                    }
                for (IVoiceChannel c : message.getGuild().getVoiceChannels())
                    if (c.getName().equals(params[0])) {
                        from = c;
                        break;
                    }
                for (IVoiceChannel c : message.getGuild().getVoiceChannels())
                    if (c.getName().equals(params[1])) {
                        to = c;
                        break;
                    }
                if(from == null)
                    sendMessage("Could not find channel **" + params[0] + "**", null, message.getChannel());
                else if(to == null)
                    sendMessage("Could not find channel **" + params[1] + "**", null, message.getChannel());
            } else {
                if (message.getAuthor().getConnectedVoiceChannels().size() == 0) {
                    sendMessage("**Make sure you are in the channel you want to populate!**", null, message.getChannel());
                    return;
                }
                for (IVoiceChannel c : client.getConnectedVoiceChannels())
                    if (message.getGuild().getVoiceChannelByID(c.getID()) != null) {
                        from = c;
                        break;
                    }
                to = message.getAuthor().getConnectedVoiceChannels().get(0);

                if (from == null) {
                    sendMessage("**Make sure the bot is the channel that you want to migrate from!**.", null, message.getChannel());
                    return;
                }
            }
            List<IUser> users = from.getConnectedUsers();
            moveUsers(users, to);
            sendMessage("Successfully moved **" + (users.size() - 1) + "** guild members from **" + from + "** to **" + to + "**", null, message.getChannel());
        });
        new Command("Bring Users", "bring", "Brings all users in a server to you.", 1, (message, params) -> {
            if (message.getAuthor().getConnectedVoiceChannels().size() == 0) {
                sendMessage("**You need to be in a voice channel to summon everyone.**", null, message.getChannel());
                return;
            }

            IVoiceChannel channel = message.getAuthor().getConnectedVoiceChannels().get(0);
            for (IUser user : message.getGuild().getUsers())
                if (user.getConnectedVoiceChannels().size() == 1)
                    user.moveToVoiceChannel(channel);

            sendMessage("Moved everyone to **" + channel.getName() + "**.", null, message.getChannel());
        });
        new Command("Disconnect User", "disconnect", "Move the list of users to the afk channel. Use @mentions.", 1, (message, params) -> {
            for (IUser user : message.getMentions()) {
                user.moveToVoiceChannel(message.getGuild().getAFKChannel());
                sendMessage("Moved **" + user.getName() + "** to the afk channel.", null, message.getChannel());
            }
        });
        new Command("Bring AFKs", "unafk", "Brings all users in the afk channel to you.", 1, (message, params) -> {
            if (message.getAuthor().getConnectedVoiceChannels().size() == 0) {
                sendMessage("**You need to be in a voice channel to summon everyone.**", null, message.getChannel());
                return;
            }

            IVoiceChannel channel = message.getAuthor().getConnectedVoiceChannels().get(0);
            for (IUser user : message.getGuild().getAFKChannel().getConnectedUsers())
                user.moveToVoiceChannel(channel);

            sendMessage("Moved everyone in the afk channel to **" + channel.getName() + "**.", null, message.getChannel());
        });
		new Command("Mass AFK", "mafk", "Move everyone in your server to the afk channel.", 1, (message, params) -> {
			IVoiceChannel channel = message.getGuild().getAFKChannel();
			for (IUser user : message.getGuild().getUsers())
			    if(!user.equals(message.getAuthor()) && user.getConnectedVoiceChannels().size() > 0)
				    user.moveToVoiceChannel(channel);
			sendMessage("Moved everyone to **" + channel.getName() + "**.", null, message.getChannel());
		});
        new Command("Summon", "summon", "Summons the bot to your voice channel.", 0, (message, params) -> {
            if (guilds.getGuild(message.getGuild()).isBotLocked()) {
                sendMessage("**SwagBot is currently locked.**", null, message.getChannel());
                return;
            }
            if (message.getAuthor().getConnectedVoiceChannels().size() == 0) {
                sendMessage("**You need to be in a voice channel to summon the bot.**", null, message.getChannel());
                return;
            }

            IVoiceChannel voicechannel = message.getAuthor().getConnectedVoiceChannels().get(0);
            voicechannel.join();

            sendMessage("Joined **" + voicechannel.getName() + "**.", null, message.getChannel());
        });
        new Command("Leave Channel", "leave", "Kicks the bot from the current voice channel.", 1, (message, params) -> {
            if (guilds.getGuild(message.getGuild()).isBotLocked()) {
                sendMessage("**SwagBot is currently locked.**", null, message.getChannel());
                return;
            }
            for (IVoiceChannel c : client.getConnectedVoiceChannels())
                if (message.getGuild().getVoiceChannelByID(c.getID()) != null) {
                    message.getGuild().getVoiceChannelByID(c.getID()).leave();
                    sendMessage("Left **" + message.getGuild().getVoiceChannelByID(c.getID()).getName() + "**.", message.getAuthor(), message.getChannel());
                    return;
                }
            sendMessage("**The bot is not currently in a voice channel.**", null, message.getChannel());
        });
        new Command("Set Volume", "volume", "Sets the volume of the bot.", 0, (message, params) -> {
            if (guilds.getGuild(message.getGuild()).isBotLocked()) {
                sendMessage("**SwagBot is currently locked.**", null, message.getChannel());
                return;
            }
            if (params.length == 0)
                sendMessage("Volume is currently set to **" + AudioPlayer.getAudioPlayerForGuild(message.getGuild()).getVolume() * 100 + "**", null, message.getChannel());
            else {
                float vol = 0;
                try {
                    vol = Float.parseFloat(params[0]);
                } catch (NumberFormatException e) {
                    sendMessage("Amount must be a number.", null, message.getChannel());
                }
                if (vol < 0 || vol > 100)
                    sendMessage("Invalid volume level, must be 0-100.", null, message.getChannel());
                else {
                    setVolume(vol, message.getGuild());
                    sendMessage("Set volume to **" + vol + "**", null, message.getChannel());
                }
            }
        });
        new Command("Playlist", "playlist", "Create, add to, queue, and delete playlists.\nUsage: ~playlist <action> <playlist> [param]\nActions: create, import, add, remove, delete, queue, list, info\nex. ~playlist create Rock, ~playlist add <youtube link>, ~playlist queue rock", 0, (message, params) -> {
            if (guilds.getGuild(message.getGuild()).isBotLocked()) {
                sendMessage("**SwagBot is currently locked.**", null, message.getChannel());
                return;
            } else if(params.length == 0)
                sendMessage("Create, add to, queue, and delete playlists.\nUsage: ~playlist <action> <playlist> [param]\nActions: create, import, add, remove, delete, queue, list, info\nex. ~playlist create Rock, ~playlist add <youtube link>, ~playlist queue rock", message.getAuthor(), message.getChannel());
            boolean perms = false;
            String response = "**Type `~help playlist` if you need help with this command**";
            List<IRole> userRoles = message.getAuthor().getRolesForGuild(message.getChannel().getGuild());
            for (IRole role : userRoles)
                if (role.getName().equals("Bot Commander"))
                    perms = true;
            if (params[0].equals("load") && perms) {
                guilds.getGuild(message.getGuild()).getPlaylistManager().load();
                sendMessage("**Re-loaded all playlists**", null, message.getChannel());
                return;
            } else if (params[0].equals("save") && perms) {
                guilds.getGuild(message.getGuild()).getPlaylistManager().save();
                sendMessage("**Saved all playlists**", null, message.getChannel());
                return;
            } else if (params[0].equals("list")) {
                sendMessage("**Playlists:** " + guilds.getGuild(message.getGuild()).getPlaylistManager().toString(), null, message.getChannel());
                return;
            }
            if (params.length < 2)
                sendMessage("**Not enough parameters. Type** `" + guilds.getGuild(message.getGuild()).getCommandPrefix() + "help playlist` **to get help with this command.**", null, message.getChannel());
            String command = params[0], name = params[1];
            LocalPlaylist playlist = guilds.getGuild(message.getGuild()).getPlaylistManager().get(name);
            if (command.equals("create")) {
                guilds.getGuild(message.getGuild()).getPlaylistManager().add(new LocalPlaylist(name, false, false));
                response = "Created playlist **" + name + "**";
            }
            if (command.equals("import") && params.length == 3) {
                String id = null;
                if(params[2].contains("playlist")) {
                    id = params[2].substring(params[2].indexOf("list=") + 5);
                } else if(params[2].contains("list=")) {
                    String link = params[2];
                    String[] parts = link.split("&");
                    for(String str : parts)
                        if(str.contains("list="))
                            id = str.replace("list=", "");
                }
                if(id != null) {
                    guilds.getGuild(message.getGuild()).getPlaylistManager().add(new LocalPlaylist(name, getYouTubeVideosFromPlaylist(id), false, false));
                    response = "Imported YouTube playlist **" + params[2] + "** into playlist **" + name + "**";
                } else
                    response = "Could not get playlist id from **" + params[2] + "**";
                message.delete();
            } else if (playlist == null) {
                response = "There is no playlist with the name **" + name + "**";
            } else if (command.equals("queue")) {
                playlist.queue(message.getAuthor(), message.getGuild());
                joinChannel(message.getAuthor(), message.getGuild());
                response = "Queuing the playlist **" + playlist.getName() + "**";
            } else if (command.equals("info")) {
                response = "Songs in **" + playlist.getName() + "**:\n" + playlist.getInfo();
            } else if (command.equals("lock") && perms) {
                playlist.toggleLocked();
                response = playlist.isLocked() ? "Playlist **" + playlist.getName() + "** can no longer be edited." : "Playlist **" + playlist.getName() + "** can now be edited.";
            } else if (command.equals("perms") && perms) {
                playlist.toggleRequiresPerms();
                response = playlist.requiresPerms() ? "Playlist **" + playlist.getName() + "** now requires moderator privelages to edit." : "Playlist **" + playlist.getName() + "** no longer requires moderator privelages to edit.";
            } else if (command.equals("add") && !(playlist.requiresPerms() && !perms) && !playlist.isLocked()) {
                message.delete();
                response = playlist.add(params[2]) ? "Added **" + new YouTubeAudio(params[2]).getTitle() + "** to **" + playlist.getName() + "**" : "Playlist **" + playlist.getName() + "** already has " + new YouTubeAudio(params[2]).getTitle();
            } else if (command.equals("remove") && perms && !playlist.isLocked()) {
                playlist.remove(params[2]);
                response = "Removed " + params[2] + " from **" + playlist.getName() + "**";
            } else if (command.equals("delete") && perms && !playlist.isLocked()) {
                guilds.getGuild(message.getGuild()).getPlaylistManager().remove(name);
                response = "Deleted the playlist **" + playlist.getName() + "**";
            }
            sendMessage(response, null, message.getChannel());
        });
        new Command("Play music", "play", "Add a song or playlist to the queue.\nUsage: ~play <link or search query>. Supports YouTube, SoundCloud, and direct links. You can also type in the name and artist of a song and SwagBot will attempt to find a video for it.\nYouTube Videos cannot be more than around 1 hour long.\nMAKE SURE THE YOUTUBE PLAYLIST ISN'T PRIVATE or the bot will not be able to see it.", 0, (message, params) -> {
            if (guilds.getGuild(message.getGuild()).isBotLocked()) {
                sendMessage("**SwagBot is currently locked.**", null, message.getChannel());
                return;
            }
            if(params.length == 0)
                sendMessage("Add a song or playlist to the queue.\nUsage: ~play <link or search query>. Supports YouTube, SoundCloud, and direct links. You can also type in the name and artist of a song and SwagBot will attempt to find a video for it.\nYouTube Videos cannot be more than around 1 hour long.\nMAKE SURE THE YOUTUBE PLAYLIST ISN'T PRIVATE or the bot will not be able to see it.", message.getAuthor(), message.getChannel());
            AudioSource source = null;
            IMessage m = null;
            try {
                if(params[0].contains("youtube"))
                    if(params[0].contains("playlist")) {
                        message.delete();
                        m = sendMessage("Queuing Playlist " + params[0], null, message.getChannel());
                        for(String music : getYouTubeVideosFromPlaylist(params[0].substring(params[0].indexOf("list=") + 5)))
                            try {
                                playAudioFromAudioSource(new YouTubeAudio(music), true, message.getAuthor(), message.getGuild());
                            } catch (IOException | UnsupportedAudioFileException e) {
                                e.printStackTrace();
                            }
                        waitAndDeleteMessage(m.edit(message.getAuthor().mention() + " **Queued Playlist** " + params[0]), 25);
                        return;
                    } else if(params[0].contains("list=")) {
                        String link = params[0];
                        String[] parts = link.split("&");
                        String id = null;
                        for(String str : parts)
                            if(str.contains("list="))
                                id = str.replace("list=", "");
                        message.delete();
                        m = sendMessage("Queuing Playlist " + params[0], null, message.getChannel());
                        for(String music : getYouTubeVideosFromPlaylist(id))
                            try {
                                playAudioFromAudioSource(new YouTubeAudio(music), true, message.getAuthor(), message.getGuild());
                            } catch (IOException | UnsupportedAudioFileException e) {
                                e.printStackTrace();
                            }
                        message.delete();
                        waitAndDeleteMessage(m.edit(message.getAuthor().mention() + " **Queued Playlist** " + params[0]), 25);
                        return;
                    } else
                        source = new YouTubeAudio(params[0]);
                else if(params[0].contains("soundcloud"))
                    source = new SoundCloudAudio(params[0]);
                else if(params[0].contains("http://"))
                    source = new AudioStream(params[0]);
                else {
                    m = sendMessage("Searching Youtube...", null, message.getChannel());
                    String query = "";
                    for(String param : params)
                        query += param + "+";
                    query = query.substring(0, query.length()-1);
                    source = getVideoFromSearch(query);
                }
            } catch (NotStreamableException e) {
                waitAndDeleteMessage(sendMessage("The track you queued cannot be streamed: " + e.getUrl(), message.getAuthor(), message.getChannel()), 25);
                e.printStackTrace();
                return;
            }
            try {
                message.delete();
                if(m == null)
                    m = sendMessage("Queuing: **" + source.getTitle() + "**", null, message.getChannel());
                else
                    m.edit("Queuing: **" + source.getTitle() + "**");
                if (playAudioFromAudioSource(source, true, message.getAuthor(), message.getGuild())) {
                    joinChannel(message.getAuthor(), message.getGuild());
                    waitAndDeleteMessage(m.edit(message.getAuthor().mention() + " Queued **" + source.getTitle() + "**"), 25);
                } else
                    m.edit(message.getAuthor().mention() + " Could not queue: **" + source.getTitle() + "**");
            } catch (IOException | UnsupportedAudioFileException e) {
                e.printStackTrace();
            }
        });
        new Command("Skip", "skip", "Skips the current song in the playlist.", 0, (message, params) -> {
            if (guilds.getGuild(message.getGuild()).hasUserSkipped(message.getAuthor().getID())) {
                sendMessage("**You already voted to skip this song.**", message.getAuthor(), message.getChannel());
                return;
            }
            guilds.getGuild(message.getGuild()).addSkipID(message.getAuthor());
            if (guilds.getGuild(message.getGuild()).numUntilSkip() == 0 || message.getAuthor().getID().equals("97341976214511616")) {
                AudioTrack track = (AudioTrack) AudioPlayer.getAudioPlayerForGuild(message.getGuild()).getCurrentTrack();
                AudioPlayer.getAudioPlayerForGuild(message.getGuild()).skip();
                guilds.getGuild(message.getGuild()).resetSkipStats();
                sendMessage("Skipped **" + track.getTitle() + "**", null, message.getChannel());
            } else
                sendMessage("**" + guilds.getGuild(message.getGuild()).numUntilSkip() + "** more votes needed to skip the current song.", null, message.getChannel());
        });
        new Command("Shuffle", "shuffle", "Shuffles the queue.", 1, (message, params) -> {
            if (guilds.getGuild(message.getGuild()).isBotLocked()) {
                sendMessage("**SwagBot is currently locked.**", null, message.getChannel());
                return;
            }
            AudioPlayer.getAudioPlayerForGuild(message.getGuild()).shuffle();
            sendMessage("**Shuffled the playlist.**", null, message.getChannel());
        });
        new Command("Pause", "pause", "Pause the queue.", 1, (message, params) -> {
            if (guilds.getGuild(message.getGuild()).isBotLocked()) {
                sendMessage("**SwagBot is currently locked.**", null, message.getChannel());
                return;
            }
            AudioPlayer.getAudioPlayerForGuild(message.getGuild()).setPaused(true);
            sendMessage("**Paused the playlist.**", null, message.getChannel());
        });
        new Command("Resume", "resume", "Resume playback.", 1, (message, params) -> {
            if (guilds.getGuild(message.getGuild()).isBotLocked()) {
                sendMessage("**SwagBot is currently locked.**", null, message.getChannel());
                return;
            }
            AudioPlayer.getAudioPlayerForGuild(message.getGuild()).setPaused(false);
            sendMessage("**Resumed the playlist.**", null, message.getChannel());
        });
        new Command("Clear Queue", "clear", "Clears the queue.", 1, (message, params) -> {
            if (guilds.getGuild(message.getGuild()).isBotLocked()) {
                sendMessage("**SwagBot is currently locked.**", null, message.getChannel());
                return;
            }
            AudioPlayer player = AudioPlayer.getAudioPlayerForGuild(message.getGuild());
            if (player.getPlaylistSize() == 0)
                sendMessage("**No songs to clear.**", null, message.getChannel());
            else {
                player.clear();
                sendMessage("**Cleared the queue.**", null, message.getChannel());
            }
        });
        new Command("Queue", "queue", "Displays the song queue.", 0, (message, params) -> {
            AudioPlayer player = AudioPlayer.getAudioPlayerForGuild(message.getGuild());
            if(player.getPlaylist().size() == 0) {
                sendMessage("There are no songs in the queue!", null, message.getChannel());
                return;
            }
            String str = " There are currently **" + (player.getPlaylistSize() - 1) + "** song(s) in queue.\n";
            String name;
            if(((AudioTrack) player.getPlaylist().get(0)).getUser() == null)
                name = "Unknown";
            else
                name = ((AudioTrack) player.getPlaylist().get(0)).getUser().getName();
            str += "Currently Playing: **" + ((AudioTrack) player.getPlaylist().get(0)).getTitle() + "** (**" + name + "**)\n";
            for (int i = 1; i < player.getPlaylist().size(); i++) {
                if(((AudioTrack) player.getPlaylist().get(i)).getUser() == null)
                    name = "Unknown";
                else
                    name = ((AudioTrack) player.getPlaylist().get(i)).getUser().getName();
                String s = "**(" + i + ")** - " + ((AudioTrack) player.getPlaylist().get(i)).getTitle() + " (**" + name + "**)\n";
                if ((str + s).length() > 1800)
                    break;
                str += s;

            }
            sendMessage(str, null, message.getChannel());
        });
        new Command("Roll a Dice", "dice", "Rolls a number on a dice from 1 to the amount you put. Default 6.", 0, (message, params) -> {
            int max = 6;
            if (!(params.length == 0))
                try {
                    max = Integer.parseInt(params[0]);
                } catch (NumberFormatException e) {
                    sendMessage("Amount must be a number.", null, message.getChannel());
                }
            sendMessage("You rolled a **" + (new Random().nextInt(max) + 1) + "**", null, message.getChannel());
        });
        new Command("Random Quote", "quote", "Gives you a random quote.", 0, (message, params) -> {
            RandomQuote quote;
            if ((int) (Math.random() * 2) == 1)
                quote = new RandomQuote("movies");
            else
                quote = new RandomQuote("famous");
            sendMessage("*\"" + quote.getQuote() + "\"*\n\t-**" + quote.getAuthor() + "**", null, message.getChannel());
        });
        new Command("8 Ball", "8ball", "Gives you a prediction to your question.", 0, (message, params) -> {
            sendMessage(new R8Ball().getResponse(), null, message.getChannel());
        });
        new Command("Random Joke", "joke", "Gives you a random joke.", 0, (message, params) -> {
            IRandomJoke joke = new CNJoke();
            sendMessage(joke.getJoke(), null, message.getChannel());
        });
        new Command("Choose From Group", "choose", "<option 1> | <option 2> [| [option 3]]\nRandomly picks one of the options given. Must provide at least 2 options.", 0, (message, params) -> {
            if(params.length == 0)
                sendMessage("<option 1> | <option 2> [| [option 3]]\nRandomly picks one of the options given. Must provide at least 2 options.", message.getAuthor(), message.getChannel());
            String[] choices = Utils.delimitWithoutEmpty(Utils.getContent(params, 0), "\\|");
            if(choices.length < 2)
                sendMessage("<option 1> | <option 2> [| [option 3]]\nRandomly picks one of the options given. Must provide at least 2 options.", message.getAuthor(), message.getChannel());
            sendMessage("I choose **" + choices[new Random().nextInt(choices.length)] + "**", null, message.getChannel());
        });
        new Command("Random Cat", "cat", "Gives you a random cat picture.", 0, (message, params) -> {
            sendMessage(new RandomCat().getUrl(), null, message.getChannel());
        });
        new Command("Urban Dictionary Lookup", "define", "Looks up a term on urban dictionary.", 0, (message, params) -> {
            String term = "";
            for (String s : params)
                term += s + " ";
            UrbanDefinition def = new UrbanDefinition(term);
            sendMessage("Term Lookup: **" + def.getTerm() + "** " + def.getLink() + "\n```\nDefinition: " + def.getDefinition() + "\nExample: " + def.getExample() + "```", null, message.getChannel());
        });
        new Command("Fight", "fight", "Make multiple users fight!\nUse @mention to list users to fight.", 0, (message, params) -> {
            List<IUser> users = new ArrayList<>();
            for(IUser user : message.getMentions())
                users.add(user);
            for(IRole role : message.getRoleMentions())
                for(IUser user : message.getGuild().getUsersByRole(role))
                    users.add(user);
            if(message.mentionsEveryone())
                users = message.getGuild().getUsers();
            if(users.size() == 1) {
                sendMessage(users.get(0).mention() + " needs at least one other person to fight!", null, message.getChannel());
                return;
            } else if(users.size() == 0) {
            	sendMessage("You need to mention users that will fight!", null, message.getChannel());
                return;
            }
            sendMessage("**Let the brawl begin!**", null, message.getChannel());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for(int i = 0; i < users.size(); i++) {
                String str = "";
                for(int j = 0; j < users.size(); j++) {
                    if(j < users.size()-1)
                        str += "**" + users.get(j).getName() + "**, ";
                    else
                        str += "and **" + users.get(j).getName() + "** are fighting!";
                }
                IMessage m = sendMessage(str, null, message.getChannel());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                List<IUser> u = users;
                RequestBuffer.request(() -> {
                    try {
                        IUser dead;
                        do {
                            dead = u.get(new Random().nextInt(u.size()));
                        } while (dead.getID().equals("148604482492563456"));
                        u.remove(dead);
                        IUser killer = u.get(new Random().nextInt(u.size()));
                        String result = prefs.getFightSituations()[new Random().nextInt(prefs.getFightSituations().length)];
                        result = result.replace("${killed}", "**" + dead.getDisplayName(message.getGuild()) + "**");
                        result = result.replace("${killer}", "**" + killer.getDisplayName(message.getGuild()) + "**");
                        m.edit(result);
                    } catch (MissingPermissionsException | DiscordException e) {
                        e.printStackTrace();
                    }
                });
                if(users.size() == 1) {
                    sendMessage(users.get(0).mention() + " **won the brawl!**", null, message.getChannel());
                    break;
                }
                i--;
            }
        });
        new Command("Let Me Google that for You", "lmgtfy", "Google anything.", 0, (message, params) -> {
            try {
                sendMessage("http://www.lmgtfy.com/?q=" + URLEncoder.encode(message.getContent().substring(8).toLowerCase(), "UTF-8"), null, message.getChannel());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        });
        new Command("Stack Overflow Search", "stackoverflow", "Search for issues on StackOverflow.com", 0, (message, params) -> {
            try {
                sendMessage("http://stackoverflow.com/search?q=" + URLEncoder.encode(message.getContent().substring(15).toLowerCase(), "UTF-8"), null, message.getChannel());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        });
        new Command("Create StrawPoll", "strawpoll", "<title> | <option 1> | <option 2> [| [option 3]]\nCreate a strawpoll with the title and options. You must have at least two options and no more than 30.", 0, (message, params) -> {
            if(params.length < 1)
                sendMessage("Not enough parameters. Type ~help strawpoll for help with this command.", null, message.getChannel());
            String[] choices = Utils.delimitWithoutEmpty(Utils.getContent(params, 0), "\\|");
            if (choices.length < 3 || choices.length > 31)
                sendMessage("Invalid parameters. Type ~help strawpoll for help with this command.", null, message.getChannel());
            for (int i = 0; i < choices.length; i++) {
                choices[i] = choices[i].trim().replace("\n", "");
            }

            JSONObject json = new JSONObject();
            json.put("title", choices[0]);
            json.put("options", new JSONArray(Arrays.copyOfRange(choices, 1, choices.length)));
            json.put("dupcheck", "normal");
            json.put("multi", false);
            json.put("captcha", true);

            HttpResponse<String> httpResponse = Unirest.post("http://www.strawpoll.me/api/v2/polls").body(json.toString()).asString();
            String body = httpResponse.getBody();
            int status = httpResponse.getStatus();
            String statusText = httpResponse.getStatusText();

            try {
                JSONObject response = new JSONObject(body);

                if (status != 200) {
                    sendMessage("I couldn't process your strawpoll: `" + status + " " + statusText + "`\nResponse body:\n```json\n" + response.toString() + "\n```", message.getAuthor(), message.getChannel());
                } else {
                    int id = response.getInt("id");

                    sendMessage("__Strawpoll by " + message.getAuthor().getDisplayName(message.getGuild()) + "__\n" +
                            "Title: **" + choices[0] + "**\n" +
                            "Strawpoll link: <https://strawpoll.me/" + id + ">", null, message.getChannel());
                }
            } catch (JSONException jsone) {
                jsone.printStackTrace();
                sendMessage("Something went wrong trying to parse JSON: " + status + " " + statusText + "\n`" + jsone + "`\n```\n" + body + "\n```", message.getAuthor(), message.getChannel());
            }
        });
        /*new Command("SHOUTcast Radio", "radio", "Play a SHOUTcast radio station through SwagBot!", 0, ((message, params) -> {
            String shoutcast = "http://api.shoutcast.com/";
            HttpResponse<JsonNode> response = Unirest.get(shoutcast + "legacy/stationsearch?f=json&k=" + prefs.getShoutCastApiKey() + "&search=")
                    .header("k", prefs.getShoutCastApiKey())
                    .asJson();
            logger.info(response.getBody().toString());
            if(response.getStatus() != 200)
                sendMessage("An error occurred while contacting the SHOUTcast API:\n```" + response.getBody().toString() + "\n```", message.getAuthor(), message.getChannel());
            JSONObject json = response.getBody().getArray().getJSONObject(0);
        }));*/
    }

    private static void stop(boolean exit) throws IOException, RateLimitException, DiscordException {
        logger.info("user initiated shutdown");
        client.changeStatus(Status.game("Shutting Down"));
        guilds.saveGuildSettings();
        prefs.save();
        if (prefs.clearCacheOnShutdown())
            clearCache();
        client.logout();
        if(exit) {
            Unirest.shutdown();
            System.exit(0);
        }
    }

    private static void restart() throws RateLimitException, IOException, DiscordException, UnirestException, InterruptedException {
        logger.info("restarting");
        stop(false);
        Thread.sleep(5000);
        main(null);
    }

    private static void update() throws RateLimitException, IOException, DiscordException {
        stop(false);
        new ProcessBuilder("java", "-jar", "-Xmx1G", "update.jar").start();
        System.exit(0);
    }

    private static int clearCache() {
        File[] cache = manager.getFile(Constants.AUDIO_CACHE).listFiles();
        int count = 0;
        for (File file : cache) {
            if (file.delete()) {
                logger.info("deleted:" + file.getName());
                count++;
            } else {
                logger.info("failed:" + file.getName());
            }
        }
        if (count < cache.length)
            logger.info("cleared:" + count + "/" + cache.length);
        else
            logger.info("cleared:" + count);
        return count;
    }

    private static IVoiceChannel getCurrentChannelForGuild(IGuild guild) {
        for (IVoiceChannel c : client.getConnectedVoiceChannels())
            if (guild.getVoiceChannelByID(c.getID()) != null) {
                return c;
        }
        return null;
    }

    private static IVoiceChannel joinChannel(IUser user, IGuild guild) throws MissingPermissionsException {
        setVolume(guilds.getGuild(guild).getVolume(), guild);
        IVoiceChannel channel = getCurrentChannelForGuild(guild);
        if(channel != null)
            return channel;
        for (IVoiceChannel c : guild.getVoiceChannels())
            if (user.getConnectedVoiceChannels().size() > 0 && c.getID().equals(user.getConnectedVoiceChannels().get(0).getID())) {
                c.join();
                return c;
            }
        guild.getVoiceChannels().get(0).join();
        return guild.getVoiceChannels().get(0);
    }

    public static boolean playAudioFromAudioSource(AudioSource source, boolean shouldAnnounce, IUser user, IGuild guild) throws IOException, UnsupportedAudioFileException {
        AudioPlayer player = AudioPlayer.getAudioPlayerForGuild(guild);
        try {
            player.queue(source.getAudioTrack(user, shouldAnnounce));
        } catch (YouTubeDLException e) {
            e.printStackTrace();
            return false;
        } catch (FFMPEGException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    // Change AudioPlayer volume for guild
    public static void setVolume(float vol, IGuild guild) {
        AudioPlayer player = AudioPlayer.getAudioPlayerForGuild(guild);
        player.setVolume(vol / 100);
        guilds.getGuild(guild).setVolume((int) vol);
    }

    public static IMessage sendMessage(String message, IUser user, IChannel channel) {
        return RequestBuffer.request(() -> {
            try {
                if (user != null)
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
            for (IUser user : users) {
                try {
                    user.moveToVoiceChannel(to);
                } catch (DiscordException | MissingPermissionsException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static List<String> getYouTubeVideosFromPlaylist(String id) throws UnirestException {
        List<String> music = new ArrayList<>();
        HttpResponse<JsonNode> response = Unirest.get("https://www.googleapis.com/youtube/v3/playlistItems?" +
                "part=contentDetails" +
                "&maxResults=50" +
                "&playlistId=" + id +
                "&key=" + App.prefs.getGoogleAuthKey()).asJson();
        JSONObject json = response.getBody().getArray().getJSONObject(0);
        String nextPage = null;
        if(json.has("nextPageToken"))
            nextPage = json.getString("nextPageToken");
        for(Object obj : json.getJSONArray("items"))
            if(obj instanceof JSONObject)
                music.add("https://www.youtube.com/watch?v=" + ((JSONObject) obj).getJSONObject("contentDetails").getString("videoId"));
        while(nextPage != null) {
            response = Unirest.get("https://www.googleapis.com/youtube/v3/playlistItems?" +
                    "part=contentDetails" +
                    "&maxResults=50" +
                    "&playlistId=" + id +
                    "&pageToken=" + nextPage +
                    "&key=" + App.prefs.getGoogleAuthKey()).asJson();
            json = response.getBody().getArray().getJSONObject(0);
            if(json.has("nextPageToken"))
                nextPage = json.getString("nextPageToken");
            else
                nextPage = null;
            for(Object obj : json.getJSONArray("items"))
                if(obj instanceof JSONObject)
                    music.add("https://www.youtube.com/watch?v=" + ((JSONObject) obj).getJSONObject("contentDetails").getString("videoId"));
        }
        return music;
    }

    private static YouTubeAudio getVideoFromSearch(String search) throws UnirestException {
        HttpResponse<JsonNode> response = Unirest.get("https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=10&q=" + search + "&key=" + prefs.getGoogleAuthKey()).asJson();
        JSONObject json = response.getBody().getArray().getJSONObject(0);
        return new YouTubeAudio("https://youtube.com/watch?v=" + json.getJSONArray("items").getJSONObject(0).getJSONObject("id").getString("videoId"));
    }
}