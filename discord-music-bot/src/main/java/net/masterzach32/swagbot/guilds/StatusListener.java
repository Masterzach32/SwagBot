package net.masterzach32.swagbot.guilds;

import sx.blah.discord.handle.impl.events.StatusChangeEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StatusListener {

    private boolean enabled;
    private String defaultChannel;
    private HashMap<String, String> entries;
    private transient IGuild guild;

    public StatusListener(IGuild guild, boolean enabled) {
        this.guild = guild;
        this.enabled = enabled;
        entries = new HashMap<>();
    }

    public StatusListener(IGuild guild, boolean enabled, HashMap<String, String> entries) {
        this.guild = guild;
        this.enabled = enabled;
        this.entries = entries;
    }

    public void setDefault(IVoiceChannel channel) {
        defaultChannel = channel.getID();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void addEntry(String game, IVoiceChannel channel) {
        entries.put(game, channel.getID());
    }

    public boolean removeEntry(String game) {
        return entries.remove(game) != null;
    }

    public boolean passEvent(StatusChangeEvent event) {
        if(!enabled)
            return false;
        IUser user = event.getUser();
        String oldStatus = event.getOldStatus().getStatusMessage();
        String newStatus = event.getNewStatus().getStatusMessage();
        if(!user.isBot() && guild.getUsers().contains(user)) {
            if(user.getConnectedVoiceChannels().stream().findFirst().get() != null) {
                try {
                    if (entries.containsKey(newStatus))
                        user.moveToVoiceChannel(guild.getVoiceChannelByID(entries.get(newStatus)));
                    else if(newStatus == null && !getGames().stream().filter((game) -> game.equals(oldStatus)).findFirst().orElse("").equals(""))
                        user.moveToVoiceChannel(guild.getVoiceChannelByID(defaultChannel));
                } catch (DiscordException | RateLimitException | MissingPermissionsException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    private List<String> getGames() {
        List<String> games = new ArrayList<>();
        entries.forEach((game, channel) -> games.add(game));
        return games;
    }
}