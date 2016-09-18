package net.masterzach32.swagbot;

import java.io.IOException;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mashape.unirest.http.*;
import com.mashape.unirest.http.exceptions.UnirestException;

import net.masterzach32.swagbot.api.NSFWFilter;
import net.masterzach32.swagbot.commands.Command;
import net.masterzach32.swagbot.music.AudioTrack;
import net.masterzach32.swagbot.utils.Constants;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.*;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.handle.obj.IMessage.*;
import sx.blah.discord.util.*;
import sx.blah.discord.util.audio.events.*;

public class EventHandler {

    public static final Logger logger = LoggerFactory.getLogger(EventHandler.class);

    @EventSubscriber
    public void onGuildCreateEvent(GuildCreateEvent event) {
        IGuild guild = event.getGuild();
        try {
            App.guilds.loadGuild(guild);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @EventSubscriber
    public void onReady(ReadyEvent event) throws MissingPermissionsException, RateLimitException, DiscordException, UnirestException {
        for (IGuild guild : event.getClient().getGuilds())
            App.setVolume(App.guilds.getGuild(guild).getVolume(), guild);
        event.getClient().changeStatus(Status.game("Queue some music!"));
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
    public void onMessageEvent(MessageReceivedEvent event) throws MissingPermissionsException, RateLimitException, DiscordException, UnirestException {
        String message = event.getMessage().getContent();

        if (message.length() < 1)
            return;

        if (event.getMessage().getChannel().isPrivate()) {
            try {
                if (event.getMessage().getContent().contains(Constants.DEFAULT_COMMAND_PREFIX + "help")) {
                    String identifier;
                    String[] params = {""};
                    if (message.indexOf(' ') > 0) {
                        identifier = message.substring(1, message.indexOf(' '));
                        params = message.substring(message.indexOf(' ') + 1).split(" ");
                    } else {
                        identifier = message.substring(1);
                    }

                    for (Command command : Command.commands)
                        if (command.getIdentifier().equals(identifier))
                            command.execute(event.getMessage(), params);
                } else
                    App.client.getOrCreatePMChannel(event.getMessage().getAuthor()).sendMessage("**SwagBot** does not currently support DM commands. The only command available to DMs is ``" + Constants.DEFAULT_COMMAND_PREFIX + "help``");
            } catch (RateLimitException | MissingPermissionsException | DiscordException e) {
                e.printStackTrace();
            }
            return;
        }

        if (event.getMessage().getChannel().getID().equals("97342233241464832")) {
            if (!event.getMessage().getEmbedded().isEmpty()) {
                App.client.getOrCreatePMChannel(event.getMessage().getAuthor()).sendMessage("Please follow the rules and dont post links in #chat.");
                event.getMessage().delete();
                return;
            }
        }


        if (App.guilds.getGuild(event.getMessage().getGuild()).isNSFWFilterEnabled()) {
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

        String identifier;
        String[] params;
        if (message.indexOf(App.guilds.getGuild(event.getMessage().getGuild()).getCommandPrefix()) == 0) {
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
    public void onTrackStartEvent(TrackStartEvent event) throws RateLimitException, DiscordException, MissingPermissionsException {
        event.getClient().changeStatus(Status.game(((AudioTrack) event.getPlayer().getCurrentTrack()).getTitle() + " - " + event.getPlayer().getGuild().getName()));
        if (((AudioTrack) event.getPlayer().getCurrentTrack()).shouldAnnounce())
            App.client.getOrCreatePMChannel(((AudioTrack) event.getPlayer().getCurrentTrack()).getUser()).sendMessage("Your song, **" + ((AudioTrack) event.getPlayer().getCurrentTrack()).getTitle() + "** is now playing in **" + event.getPlayer().getGuild().getName() + "!**");
        App.guilds.getGuild(event.getPlayer().getGuild()).resetSkipStats();
    }

    @EventSubscriber
    public void onTrackFinishEvent(TrackFinishEvent event) {
        if (event.getPlayer().getPlaylistSize() == 0)
            event.getClient().changeStatus(Status.game("Queue some music!"));
    }

    @EventSubscriber
    public void onPauseStateChangeEvent(PauseStateChangeEvent event) {
        if (event.getNewPauseState())
            event.getClient().changeStatus(Status.game("Paused"));
        else
            event.getClient().changeStatus(Status.game(((AudioTrack) event.getPlayer().getCurrentTrack()).getTitle()));
    }
}