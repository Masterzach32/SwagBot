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

import com.mashape.unirest.http.exceptions.UnirestException;

import net.masterzach32.commands4j.Commands;
import net.masterzach32.swagbot.commands.admin.AnnounceTrackCommand;
import net.masterzach32.swagbot.commands.admin.ChangePrefixCommand;
import net.masterzach32.swagbot.commands.admin.NSFWCommand;
import net.masterzach32.swagbot.commands.admin.NickCommand;
import net.masterzach32.swagbot.commands.dev.*;
import net.masterzach32.swagbot.commands.fun.*;
import net.masterzach32.swagbot.commands.mod.*;
import net.masterzach32.swagbot.commands.music.*;
import net.masterzach32.swagbot.commands.normal.*;
import net.masterzach32.swagbot.commands.test.PingCommand;
import net.masterzach32.swagbot.guilds.GuildManager;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mashape.unirest.http.Unirest;

import net.masterzach32.swagbot.commands.*;
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

        HttpClient httpClient = HttpClients.custom()
                .disableCookieManagement()
                .build();
        Unirest.setHttpClient(httpClient);

        cmds = new Commands()
                .add(new HelpCommand())
                .add(new ShutdownCommand())
                .add(new UpdateCommand())
                .add(new ThreadCommand())
                .add(new PingCommand())
                .add(new ReloadCommand(prefs))
                .add(new NSFWCommand())
                .add(new ChangePrefixCommand())
                .add(new StatusCommand())
                .add(new RefreshCommand())
                .add(new BanCommand())
                .add(new PardonCommand())
                .add(new SoftBanCommand())
                .add(new KickCommand())
                .add(new PruneCommand())
                .add(new MigrateCommand())
                .add(new BringCommand())
                .add(new DisconnectCommand())
                .add(new AFKCommand())
                .add(new SummonCommand())
                .add(new LeaveCommand())
                .add(new VolumeCommand())
                .add(new GameCommand())
                .add(new PlaylistCommand())
                .add(new PlayCommand())
                .add(new AnnounceTrackCommand())
                .add(new NickCommand())
                .add(new ReplayCommand())
                .add(new SkipCommand())
                .add(new SkipToCommand())
                .add(new ShuffleCommand())
                .add(new PauseCommand())
                .add(new ResumeCommand())
                .add(new LoopCommand())
                .add(new QueueCommand())
                .add(new DiceCommand())
                .add(new QuoteCommand())
                .add(new R8BallCommand())
                .add(new JokeCommand())
                .add(new ChooseCommand())
                .add(new CatCommand())
                .add(new UrbanDictionaryCommand())
                .add(new FightCommand(prefs))
                .add(new LmgtfyCommand())
                .add(new StackoverflowCommand());
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

    public static IVoiceChannel joinChannel(IUser user, IGuild guild) throws MissingPermissionsException {
        //setVolume(guilds.getGuildSettings(guild).getVolume(), guild);
        IVoiceChannel channel = guild.getConnectedVoiceChannel();
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
}