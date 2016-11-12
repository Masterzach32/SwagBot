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

import java.io.*;
import java.util.*;
import java.util.function.Consumer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.masterzach32.swagbot.App;
import net.masterzach32.swagbot.utils.ConstantsKt;
import org.json.JSONObject;
import sx.blah.discord.handle.obj.IGuild;

public class GuildManager {

	private List<GuildSettings> guilds;

	public GuildManager() {
		guilds = new ArrayList<>();
	}

	public GuildSettings loadGuild(IGuild guild) {
		App.manager.mkdir(ConstantsKt.getGUILD_SETTINGS() + guild.getID() + "/playlists/");
		File prefs = new File(ConstantsKt.getGUILD_SETTINGS() + guild.getID() + "/" + ConstantsKt.getGUILD_JSON());
		Gson gson = new Gson();
		GuildSettings temp;
        String json = null;
		try {
			if (!prefs.exists()) {
				prefs.createNewFile();
				BufferedWriter fout = new BufferedWriter(new FileWriter(ConstantsKt.getGUILD_SETTINGS() + guild.getID() + "/" + ConstantsKt.getGUILD_JSON()));
				fout.write(new GsonBuilder().setPrettyPrinting().create().toJson(new GuildSettings(guild,
						ConstantsKt.getDEFAULT_COMMAND_PREFIX(),
                        3,
                        50,
                        false,
                        false,
                        true,
                        false,
                        null,
                        new ArrayList<>(),
                        new StatusListener(guild, false))));
				fout.close();
			}

			RandomAccessFile fin = new RandomAccessFile(ConstantsKt.getGUILD_SETTINGS() + guild.getID() + "/" + ConstantsKt.getGUILD_JSON(), "r"); // "r" = open file for reading only
			byte[] buffer = new byte[(int) fin.length()];
			fin.readFully(buffer);
			fin.close();

			json = new String(buffer);
		} catch (IOException e) {

		}

        JSONObject obj = new JSONObject(json);

        temp = new GuildSettings(
                guild,
                obj.has("commandPrefix") ? obj.getString("commandPrefix").charAt(0) : '~',
                obj.has("maxSkips") ? obj.getInt("maxSkips") : 3,
                obj.has("volume") ? obj.getInt("volume") : 50,
                obj.has("botLocked") ? obj.getBoolean("botLocked") : false,
                obj.has("nsfwFilter") ? obj.getBoolean("nsfwFilter") : false,
                obj.has("announce") ? obj.getBoolean("announce") : true,
                obj.has("changeNick") ? obj.getBoolean("changeNick") : false,
                obj.has("lastChannel") ? obj.getString("lastChannel") : "",
                obj.has("queue") ? gson.fromJson(obj.get("queue").toString(), ArrayList.class) : new ArrayList<>(),
                obj.has("listener") ? new StatusListener(guild, obj.getJSONObject("listener").getBoolean("enabled"), gson.fromJson(obj.getJSONObject("listener").get("entries").toString(), HashMap.class)) : new StatusListener(guild, false)
        );

		guilds.add(temp);
        temp.getPlaylistManager().load();

		return temp;
	}

	public void applyGuildSettings() {
        for(int i = 0; i < guilds.size(); i++) {
            if(guilds.get(i) != null)
			    guilds.get(i).applySettings().saveSettings();
            else
                guilds.remove(i);
        }
	}

	public GuildSettings removeGuild(IGuild guild) {
		for(int i = 0; i < guilds.size(); i++)
			if(guilds.get(i) != null && guilds.get(i).getIGuild().getID().equals(guild.getID()))
                return guilds.remove(i);
        return null;
	}

	public void saveGuildSettings() {
		for(int i = 0; i < guilds.size(); i++) {
			if(guilds.get(i) != null)
            	guilds.get(i).saveSettings();
			else
				guilds.remove(i);
		}
	}

	public GuildSettings getGuildSettings(IGuild guild) {
		for(int i = 0; i < guilds.size(); i++)
			if(guilds.get(i) != null && guilds.get(i).getIGuild().getID().equals(guild.getID()))
				return guilds.get(i);
		return loadGuild(guild);
	}

	public void forEach(Consumer<? super GuildSettings> action) {
        Iterator<GuildSettings> iterator = guilds.iterator();
        iterator.forEachRemaining(action);
    }
}