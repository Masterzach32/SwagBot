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

import java.io.IOException;

import com.mashape.unirest.http.exceptions.UnirestException;

import net.masterzach32.commands4j.CommandManager;
import net.masterzach32.swagbot.commands.admin.*;
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
import sx.blah.discord.api.*;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.*;

public class App {

    public static final Logger logger = LoggerFactory.getLogger(App.class);

    public static IDiscordClient client;
    public static BotConfig prefs;
    public static GuildManager guilds;
    public static CommandManager cmds;
    public static StatManager stats;

    public static void main(String[] args) throws DiscordException, IOException, UnirestException, RateLimitException {
        // https://discordapp.com/oauth2/authorize?client_id=217065780078968833&scope=bot&permissions=8
        // beta https://discordapp.com/oauth2/authorize?client_id=219554475055120384&scope=bot&permissions=8

        // load bot settings
        prefs = new BotConfig();
        prefs.load();
        // load guild-specific settings
        guilds = new GuildManager();

        stats = StatManagerKt.load(prefs.getStatsStorage());

        client = new ClientBuilder().withToken(prefs.getDiscordAuthKey()).withShards(prefs.getShardCount()).build();
        client.getDispatcher().registerListener(new EventHandler());

        HttpClient httpClient = HttpClients.custom()
                .disableCookieManagement()
                .build();
        Unirest.setHttpClient(httpClient);

        cmds = new CommandManager()
                .add(new HelpCommand())
                .add(new ShutdownCommand())
                .add(new UpdateCommand())
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
                .add(new StackoverflowCommand())
                .add(new StrawpollCommand())
                .add(new SwagCommand())
                //.add(new CurrencyExchange(prefs))
                .add(new UrlShortenCommand())
                .add(new StatsCommand(stats))
                .add(new PermCommand())
                .add(new ClearCommand())
                .add(new InviteCommand())
                .add(new TrumpQuoteCommand())
                .add(new RockPaperScissorsCommand());

        client.login();
    }

    public static void stop(boolean exit) throws IOException, RateLimitException, DiscordException {
        logger.info("user initiated shutdown");
        client.changeStatus(Status.game("Shutting Down"));
        //stats.save(prefs.getStatsStorage()); TODO fix gson error
        prefs.save();
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