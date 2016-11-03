package net.masterzach32.swagbot.guilds;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.masterzach32.swagbot.App;
import net.masterzach32.swagbot.music.player.AudioSource;
import net.masterzach32.swagbot.utils.Constants;
import org.json.JSONObject;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.handle.obj.Status;

public class GuildManager {

	private List<GuildSettings> guilds;

	public GuildManager() {
		guilds = new ArrayList<>();
	}

	public GuildSettings loadGuild(IGuild guild) {
		App.manager.mkdir(Constants.GUILD_SETTINGS + guild.getID() + "/playlists/");
		File prefs = new File(Constants.GUILD_SETTINGS + guild.getID() + "/" + Constants.GUILD_JSON);
		Gson gson = new Gson();
		GuildSettings temp;
        String json = null;
		try {
			if (!prefs.exists()) {
				prefs.createNewFile();
				BufferedWriter fout = new BufferedWriter(new FileWriter(Constants.GUILD_SETTINGS + guild.getID() + "/" + Constants.GUILD_JSON));
				fout.write(new GsonBuilder().setPrettyPrinting().create().toJson(new GuildSettings(guild,
                        Constants.DEFAULT_COMMAND_PREFIX,
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

			RandomAccessFile fin = new RandomAccessFile(Constants.GUILD_SETTINGS + guild.getID() + "/" + Constants.GUILD_JSON, "r"); // "r" = open file for reading only
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
                obj.has("enableNSFWFilter") ? obj.getBoolean("enableNSFWFilter") : false,
                obj.has("announce") ? obj.getBoolean("announce") : true,
                obj.has("changeNick") ? obj.getBoolean("changeNick") : false,
                obj.has("lastChannel") ? obj.getString("lastChannel") : "",
                obj.has("queue") ? gson.fromJson(obj.get("queue").toString(), ArrayList.class) : new ArrayList<>(),
                obj.has("listener") ? new StatusListener(guild, obj.getJSONObject("listener").getBoolean("enabled"), gson.fromJson(obj.getJSONObject("listener").get("entries").toString(), HashMap.class)) : new StatusListener(guild, false)
        );

		/*boolean isAlreadyAdded = false;
		for(int i = 0; i < guilds.size(); i++) {
			if(guilds.get(i).getIGuild().getID().equals(temp.getIGuild().getID()))
				isAlreadyAdded = true;
		}
		if(!isAlreadyAdded)*/
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
			if(guilds.get(i) != null && guilds.get(i).getID().equals(guild.getID()))
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
			if(guilds.get(i) != null && guilds.get(i).getID().equals(guild.getID()))
				return guilds.get(i);
		return loadGuild(guild);
	}

	public void forEach(Consumer<? super GuildSettings> action) {
        Iterator<GuildSettings> iterator = guilds.iterator();
        iterator.forEachRemaining(action);
    }
}