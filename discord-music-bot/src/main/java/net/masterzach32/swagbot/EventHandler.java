package net.masterzach32.swagbot;

import java.io.IOException;

import net.masterzach32.swagbot.guilds.GuildSettings;
import net.masterzach32.swagbot.utils.exceptions.FFMPEGException;
import net.masterzach32.swagbot.utils.exceptions.NotStreamableException;
import net.masterzach32.swagbot.utils.exceptions.YouTubeDLException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mashape.unirest.http.*;
import com.mashape.unirest.http.exceptions.UnirestException;

import net.masterzach32.swagbot.api.NSFWFilter;
import net.masterzach32.swagbot.commands.Command;
import net.masterzach32.swagbot.music.player.AudioTrack;
import net.masterzach32.swagbot.utils.Constants;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.*;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.handle.obj.IMessage.*;
import sx.blah.discord.util.*;
import sx.blah.discord.util.audio.events.*;

import javax.sound.sampled.UnsupportedAudioFileException;

public class EventHandler {

    public static final Logger logger = LoggerFactory.getLogger(EventHandler.class);

    @EventSubscriber
    public void onGuildCreateEvent(GuildCreateEvent event) throws UnsupportedAudioFileException, UnirestException, FFMPEGException, NotStreamableException, YouTubeDLException, IOException, MissingPermissionsException {
        App.guilds.loadGuild(event.getGuild());
        RequestBuffer.request(() -> {
            if(event.getClient().isReady() && event.getClient().isLoggedIn())
                event.getClient().changeStatus(Status.game(event.getClient().getGuilds().size() + " servers | ~help"));
        });
    }

    @EventSubscriber
    public void onGuildLeaveEvent(GuildLeaveEvent event) throws UnsupportedAudioFileException, UnirestException, FFMPEGException, NotStreamableException, YouTubeDLException, IOException, MissingPermissionsException {
        App.guilds.removeGuild(event.getGuild());
    }

    @EventSubscriber
    public void onDiscordDisconnectEvent(DiscordDisconnectedEvent event) {
        logger.error("DISCONNECTED FROM DISCORD");
    }

    @EventSubscriber
    public void onDiscordReconnectedEvent(DiscordReconnectedEvent event) throws MissingPermissionsException, InterruptedException {
        /*for(IGuild guild : event.getClient().getGuilds()) {
            for(IVoiceChannel channel : guild.getVoiceChannels())
                for(IVoiceChannel connected : event.getClient().getConnectedVoiceChannels())
                    if(connected == channel) {
                        AudioPlayer player = AudioPlayer.getAudioPlayerForGuild(guild);
                        connected.leave();
                        Thread.sleep(500);
                        connected.join();
                    }
        }*/
    }

    @EventSubscriber
    public void onReady(ReadyEvent event) throws MissingPermissionsException, RateLimitException, DiscordException, UnirestException, InterruptedException {
        RequestBuffer.request(() -> {
            event.getClient().changeStatus(Status.game(event.getClient().getGuilds().size() + " servers | ~help"));
            App.guilds.applyGuildSettings();
        });
        if (App.prefs.shouldPostBotStats()) {
            HttpResponse<JsonNode> json = Unirest.post("https://bots.discord.pw/api/bots/" + App.prefs.getDiscordClientId() + "/stats")
                    .header("User-Agent", "SwagBot/1.0 (UltimateDoge)")
                    .header("Content-Type", "application/json")
                    .header("Authorization", App.prefs.getDBAuthKey())
                    .body(new JSONObject().put("server_count", event.getClient().getGuilds().size()))
                    .asJson();
            logger.info(json.getBody().getArray().getJSONObject(0).toString());
        }
    }

    @EventSubscriber
    public void onMessageEvent(MessageReceivedEvent event) throws MissingPermissionsException, RateLimitException, DiscordException, UnirestException, IOException, UnsupportedAudioFileException, YouTubeDLException, FFMPEGException, NotStreamableException {
        String message = event.getMessage().getContent();

        if (message.length() < 1 || event.getMessage().getAuthor().isBot())
            return;

        if (event.getMessage().getChannel().isPrivate()) {
            try {
                if (message.startsWith(Constants.DEFAULT_COMMAND_PREFIX + "help")) {
                    String[] params = new String[0];
                    if(message.length() > 5)
                        params = message.substring(6).split(" ");
                    Command.executeCommand(event.getMessage(), "help", params);
                } else
                    App.client.getOrCreatePMChannel(event.getMessage().getAuthor()).sendMessage("**SwagBot** does not currently support DM commands. The only command available to DMs is ``" + Constants.DEFAULT_COMMAND_PREFIX + "help``");
            } catch (RateLimitException | MissingPermissionsException | DiscordException e) {
                e.printStackTrace();
            }
            return;
        }

        GuildSettings g = App.guilds.getGuildSettings(event.getMessage().getGuild());

        if (g.isNSFWFilterEnabled()) {
            for (Attachment a : event.getMessage().getAttachments())
                logger.info("attachment: " + a.getUrl() + " " + a.getFilename());
            for (IEmbedded image : event.getMessage().getEmbedded()) {
                logger.info("embed: " + image.getUrl());
                if (image.getUrl() != null) {
                    NSFWFilter filter = new NSFWFilter(event.getMessage().getGuild(), image.getUrl());
                    if (filter.isNSFW()) {
                        App.client.getOrCreatePMChannel(event.getMessage().getAuthor()).sendMessage("Your image, `" + filter.getUrl() + "` which you posted in **" + event.getMessage().getGuild().getName() + "** **" + event.getMessage().getChannel() + "**, was flagged as containing NSFW content, and has been removed. If you believe this is an error, contact the server owner or one of my developers.");
                        event.getMessage().delete();
                    } else if (filter.isPartial()) {
                        App.client.getOrCreatePMChannel(event.getMessage().getAuthor()).sendMessage("Your image, `" + filter.getUrl() + "` which you posted in **" + event.getMessage().getGuild().getName() + "** **" + event.getMessage().getChannel() + "**, was flagged as containing some or partial NSFW content. Please be aware that NSFW images will be automatically deleted. If you believe this is an error, contact the server owner or one of my developers.");
                    }
                    logger.info("result: nsfw:" + filter.getRaw() + "% partial:" + filter.getPartial() + "% safe:" + filter.getSafe() + "%");
                }
            }
        }

        if (event.getMessage().getChannel().getID().equals("97342233241464832")) {
            if (!event.getMessage().getEmbedded().isEmpty() || !event.getMessage().getAttachments().isEmpty() || message.contains("http://") || message.contains("https://")) {
                App.waitAndDeleteMessage(App.sendMessage("please don't post links or attachments in " + event.getMessage().getChannel().mention(), event.getMessage().getAuthor(), event.getMessage().getChannel()), 30);
                event.getMessage().delete();
                return;
            }
        }

        String identifier;
        String[] params;
        if (message.indexOf(App.guilds.getGuildSettings(event.getMessage().getGuild()).getCommandPrefix()) == 0) {
            message = message.substring(1, message.length());
            String[] split = message.split(" ");
            identifier = split[0];
            params = new String[split.length - 1];
            for (int i = 0; i < params.length; i++) {
                params[i] = split[i + 1];
            }
            Command.executeCommand(event.getMessage(), identifier, params);
        } else if (event.getMessage().getMentions().contains(event.getClient().getOurUser()) && !event.getMessage().mentionsEveryone()) {
            String[] split = message.split(" ");
            String[] command = new String[split.length - 1];
            for (int i = 0; i < command.length; i++) {
                command[i] = split[i + 1];
            }
            identifier = command[0];
            params = new String[command.length - 1];
            for (int i = 0; i < params.length; i++) {
                params[i] = command[i + 1];
            }
            Command.executeCommand(event.getMessage(), identifier, params);
        }
    }

    @EventSubscriber
    public void onTrackStartEvent(TrackStartEvent event) throws RateLimitException, MissingPermissionsException {
        try {
            if (((AudioTrack) event.getPlayer().getCurrentTrack()).shouldAnnounce() && App.guilds.getGuildSettings(event.getPlayer().getGuild()).shouldAnnounce())
                App.client.getOrCreatePMChannel(((AudioTrack) event.getPlayer().getCurrentTrack()).getUser()).sendMessage("Your song, **" + ((AudioTrack) event.getPlayer().getCurrentTrack()).getTitle() + "** is now playing in **" + event.getPlayer().getGuild().getName() + "!**");
            if(App.guilds.getGuildSettings(event.getPlayer().getGuild()).shouldChangeNick()) {
                String track;
                if (((AudioTrack) event.getPlayer().getCurrentTrack()).getTitle().length() > 32)
                    track = ((AudioTrack) event.getPlayer().getCurrentTrack()).getTitle().substring(0, 32);
                else
                    track = ((AudioTrack) event.getPlayer().getCurrentTrack()).getTitle();
                if(track == null) {
                    logger.warn("An audio track returned a null value for getTitle() " + event.getPlayer().getCurrentTrack().toString());
                    track = "SwagBot";
                }
                event.getPlayer().getGuild().setUserNickname(event.getClient().getOurUser(), track);
            }
        } catch (DiscordException e) {
            logger.warn("Could not send message to " + ((AudioTrack) event.getPlayer().getCurrentTrack()).getUser().getName());
        }
        App.guilds.getGuildSettings(event.getPlayer().getGuild()).resetSkipStats();
    }

    @EventSubscriber
    public void onTrackFinishEvent(TrackFinishEvent event) {
        try {
            if(App.guilds.getGuildSettings(event.getPlayer().getGuild()).shouldChangeNick() && event.getPlayer().getPlaylistSize() == 0)
                event.getPlayer().getGuild().setUserNickname(event.getClient().getOurUser(), "SwagBot");
        } catch (MissingPermissionsException | DiscordException | RateLimitException e) {
            e.printStackTrace();
        }
    }

    @EventSubscriber
    public void onPauseStateChangeEvent(PauseStateChangeEvent event) {}
}