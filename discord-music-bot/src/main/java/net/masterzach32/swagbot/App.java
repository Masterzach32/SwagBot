/*
    SwagBot - A Discord Music Bot
    Copyright (C) 2016  Zachary Kozar

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc.,
    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package net.masterzach32.swagbot;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import javax.sound.sampled.UnsupportedAudioFileException;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.exceptions.UnirestException;

import net.masterzach32.commands4j.Commands;
import net.masterzach32.swagbot.commands.dev.ReloadCommand;
import net.masterzach32.swagbot.commands.dev.ShutdownCommand;
import net.masterzach32.swagbot.commands.dev.ThreadCommand;
import net.masterzach32.swagbot.commands.dev.UpdateCommand;
import net.masterzach32.swagbot.commands.test.PingCommand;
import net.masterzach32.swagbot.guilds.GuildManager;
import net.masterzach32.swagbot.utils.exceptions.*;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mashape.unirest.http.Unirest;

import net.masterzach32.swagbot.commands.*;
import net.masterzach32.swagbot.music.player.*;
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
    public static Commands cmds;

    public static void main(String[] args) throws DiscordException, IOException, UnirestException, RateLimitException {
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
        client.login();

        cmds = new Commands()
                .add(new HelpCommand())
                .add(new ShutdownCommand())
                .add(new UpdateCommand())
                .add(new ThreadCommand())
                .add(new PingCommand())
                .add(new ReloadCommand(prefs));
    }

    public static void stop(boolean exit) throws IOException, RateLimitException, DiscordException {
        logger.info("user initiated shutdown");
        client.changeStatus(Status.game("Shutting Down"));
        prefs.save();
        if (prefs.clearCacheOnShutdown())
            clearCache();
        guilds.saveGuildSettings();
        client.logout();
        if(exit) {
            Unirest.shutdown();
            logger.info("Successfully shut down SwagBot");
            System.exit(0);
        }
    }

    public static void restart() throws RateLimitException, IOException, DiscordException, UnirestException, InterruptedException {
        logger.info("restarting");
        stop(false);
        new ProcessBuilder("start-bot.bat").start();
        System.exit(0);
    }

    public static void update() throws RateLimitException, IOException, DiscordException {
        stop(false);
        new ProcessBuilder("java", "-jar", "-Xmx1G", "update.jar").start();
        System.exit(0);
    }

    private static int clearCache() {
        File[] cache = manager.getFile(ConstantsKt.getAUDIO_CACHE()).listFiles();
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
        return client.getConnectedVoiceChannels().stream().filter((iVoiceChannel -> guild.getVoiceChannels().contains(iVoiceChannel))).findFirst().orElse(null);
    }

    private static IVoiceChannel joinChannel(IUser user, IGuild guild) throws MissingPermissionsException {
        //setVolume(guilds.getGuildSettings(guild).getVolume(), guild);
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

    public static void playAudioFromAudioSource(AudioSource source, boolean shouldAnnounce, IMessage message, IUser user, IGuild guild) throws IOException, UnsupportedAudioFileException {
        if(source == null)
            return;
        AudioPlayer player = AudioPlayer.getAudioPlayerForGuild(guild);
        if (message != null)
            editMessage(message, "Queuing **" + source.getTitle() + "**");
        try {
            if (source instanceof YouTubeAudio && ((YouTubeAudio) source).isLive()) {
                waitAndDeleteMessage(editMessage(message, user.mention() + " Could not queue **" + source.getTitle() + "**: Live Streams are currently not supported!"), 120);
                return;
            } else if (source instanceof YouTubeAudio && ((YouTubeAudio) source).isDurationAnHour()) {
                waitAndDeleteMessage(editMessage(message, user.mention() + " Could not queue **" + source.getTitle() + "**: Video length must be less than 1 hour!"), 120);
                return;
            }
            player.queue(source.getAudioTrack(user, shouldAnnounce));
            joinChannel(user, guild);
            if (message != null)
                waitAndDeleteMessage(editMessage(message, user.mention() + " Queued **" + source.getTitle() + "**"), 30);
            return;
        } catch (YouTubeDLException e) {
            e.printStackTrace();
            if (message != null)
                waitAndDeleteMessage(editMessage(message, user.mention() + " Could not queue **" + source.getTitle() + "**: An error occurred while downloading the video."), 120);
            return;
        } catch (FFMPEGException e) {
            e.printStackTrace();
            if (message != null)
                waitAndDeleteMessage(editMessage(message, user.mention() + " Could not queue **" + source.getTitle() + "**: An error occurred while converting to audio stream"), 120);
            return;
        } catch (IOException | MissingPermissionsException e) {
            e.printStackTrace();
        }
        if (message != null)
            waitAndDeleteMessage(editMessage(message, user.mention() + " Could not queue **" + source.getTitle() + "**: (unknown reason)"), 120);
        return;
    }

    // Change AudioPlayer volume for guild
    public static void setVolume(float vol, IGuild guild) {
        AudioPlayer player = AudioPlayer.getAudioPlayerForGuild(guild);
        guilds.getGuildSettings(guild).setVolume((int) vol);
        player.setVolume(vol);
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

    public static IMessage editMessage(IMessage message, String contents) {
        return RequestBuffer.request(() -> {
            try {
                return message.edit(contents);
            } catch (DiscordException | MissingPermissionsException e) {
                e.printStackTrace();
            }
            return null;
        }).get();
    }

    public static void waitAndDeleteMessage(IMessage message, int seconds) {
        new Thread() {
            public void run() {
                RequestBuffer.request(() -> {
                    try {
                        Thread.sleep(seconds * 1000);
                        message.delete();
                    } catch (MissingPermissionsException | DiscordException | InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            }
        }.start();
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

    private static List<YouTubeAudio> getYouTubeVideosFromPlaylist(String id) throws UnirestException, YouTubeAPIException {
        List<YouTubeAudio> music = new ArrayList<>();
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
                music.add(new YouTubeAudio("https://www.youtube.com/watch?v=" + ((JSONObject) obj).getJSONObject("contentDetails").getString("videoId")));
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
                    music.add(new YouTubeAudio("https://www.youtube.com/watch?v=" + ((JSONObject) obj).getJSONObject("contentDetails").getString("videoId")));
        }
        return music;
    }

    private static YouTubeAudio getVideoFromSearch(String search) throws UnirestException, YouTubeAPIException {
        HttpResponse<JsonNode> response = Unirest.get("https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=50&q=" + search + "&key=" + prefs.getGoogleAuthKey()).asJson();
        if(response.getStatus() != 200)
            return null;
        JSONObject json;
        json = response.getBody().getArray().getJSONObject(0);
        if(json.has("items") && json.getJSONArray("items").length() > 0 && json.getJSONArray("items").getJSONObject(0).has("id") && json.getJSONArray("items").getJSONObject(0).getJSONObject("id").has("videoId"))
            return new YouTubeAudio("https://youtube.com/watch?v=" + json.getJSONArray("items").getJSONObject(0).getJSONObject("id").getString("videoId"));
        return null;
    }

    private static List<IUser> getUsersByRole(IGuild guild, IRole role) {
        return guild.getUsers().stream().filter((user) -> user.getRolesForGuild(guild).contains(role))
                .collect(Collectors.toList());
    }
}