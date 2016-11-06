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

    public void setDefaultChannel(IVoiceChannel channel) {
        defaultChannel = channel.getID();
    }

    public String getDefaultChannel() {
        return defaultChannel;
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

    public boolean hasEntries() {
        return entries.size() > 0;
    }

    public boolean passEvent(StatusChangeEvent event) {
        if(!enabled)
            return false;
        IUser user = event.getUser();
        String newStatus = event.getNewStatus().getStatusMessage();
        if(!user.isBot() && guild.getUsers().contains(user)) {
            if(user.getConnectedVoiceChannels().stream().findFirst().isPresent()) {
                try {
                    if (entries.containsKey(newStatus))
                        user.moveToVoiceChannel(guild.getVoiceChannelByID(entries.get(newStatus)));
                    else
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

    public String listEntries() {
        List<String> games = new ArrayList<>();
        entries.forEach((game, channel) -> games.add("**" + game + "**: " + channel));
        String str = "";
        for(String s : games)
            str += s + "\n";
        return str;
    }
}