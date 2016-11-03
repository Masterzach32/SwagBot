package net.masterzach32.swagbot.guilds;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.GsonBuilder;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.masterzach32.swagbot.App;
import net.masterzach32.swagbot.music.PlaylistManager;
import net.masterzach32.swagbot.music.player.*;
import net.masterzach32.swagbot.utils.Constants;
import net.masterzach32.swagbot.utils.exceptions.FFMPEGException;
import net.masterzach32.swagbot.utils.exceptions.NotStreamableException;
import net.masterzach32.swagbot.utils.exceptions.YouTubeAPIException;
import net.masterzach32.swagbot.utils.exceptions.YouTubeDLException;
import sx.blah.discord.handle.impl.events.StatusChangeEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;
import sx.blah.discord.util.audio.AudioPlayer;

import javax.sound.sampled.UnsupportedAudioFileException;

public class GuildSettings {

	private transient IGuild guild;
	private transient PlaylistManager playlists;
	private transient List<String> skipIDs;
	private String guildName;
	private char commandPrefix;
	private int maxSkips, volume;
	private boolean botLocked, enableNSFWFilter, announce, changeNick;
	private String lastChannel;
    private List<String> queue;
    private StatusListener listener;

	protected GuildSettings(IGuild guild, char commandPrefix, int maxSkips, int volume, boolean botLocked, boolean nsfwfilter, boolean announce, boolean changeNick, String lastChannel, List<String> queue, StatusListener listener) {
		this.guild = guild;
		playlists = new PlaylistManager(guild.getID());
		skipIDs = new ArrayList<>();
		this.guildName = guild.getName();
		this.commandPrefix = commandPrefix;
		this.maxSkips = maxSkips;
		this.volume = volume;
		this.botLocked = botLocked;
		this.enableNSFWFilter = nsfwfilter;
		this.announce = announce;
        this.changeNick = changeNick;
        this.lastChannel = lastChannel;
        this.queue = queue;
        this.listener = listener;
	}

	public String getID() {
		return guild.getID();
	}

	public void resetSkipStats() {
		skipIDs.clear();
	}

	public void addSkipID(IUser user) {
		skipIDs.add(user.getID());
	}

	public boolean hasUserSkipped(String userID) {
		return skipIDs.contains(userID);
	}

	public int numUntilSkip() {
		return maxSkips - skipIDs.size();
	}

	public int getMaxSkips() {
		return maxSkips;
	}

	public void setMaxSkips(int maxSkips) {
		this.maxSkips = maxSkips;
	}

	public int getVolume() {
		return volume;
	}

	public void setVolume(int vol) {
		this.volume = vol;
	}

	public boolean toggleBotLocked() {
		return botLocked = !botLocked;
	}

	public boolean isBotLocked() {
		return botLocked;
	}

	public PlaylistManager getPlaylistManager() {
		return playlists;
	}

	public char getCommandPrefix() {
		return commandPrefix;
	}

	public void setCommandPrefix(char prefix) {
		this.commandPrefix = prefix;
	}

	public boolean isNSFWFilterEnabled() {
		return enableNSFWFilter;
	}

	public void toggleNSFWFilter() {
		enableNSFWFilter = !enableNSFWFilter;
	}

    public boolean shouldAnnounce() {
        return announce;
    }

    public void setShouldAnnounce(boolean announce) {
        this.announce = announce;
    }

    public boolean shouldChangeNick() {
        return changeNick;
    }

    public void setChangeNick(boolean changeNick) {
        this.changeNick = changeNick;
    }

	public String getLastChannel() {
        return lastChannel;
    }

    public void setLastChannel(String id) {
        lastChannel = id;
    }

    public void setQueue(List<String> queue) {
        this.queue = queue;
    }

    public List<String> getQueue() {
        return queue;
    }

    public StatusListener getStatusListener() {
        return listener;
    }

    public boolean dispatchStatusChangedEvent(StatusChangeEvent event) {
        return listener.passEvent(event);
    }

    public IGuild getIGuild() {
		return guild;
	}

    public GuildSettings saveSettings() {
        getPlaylistManager().save();

        List<AudioPlayer.Track> tracks = getAudioPlayer().getPlaylist();
        queue = new ArrayList<>();
        for(AudioPlayer.Track track : tracks)
            if(track != null)
                queue.add(((AudioTrack) track).getUrl());

        try {
            BufferedWriter fout = new BufferedWriter(new FileWriter(Constants.GUILD_SETTINGS + guild.getID() + "/" + Constants.GUILD_JSON));
            fout.write(new GsonBuilder().setPrettyPrinting().create().toJson(this));
            fout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    public GuildSettings applySettings() {
        RequestBuffer.request(() -> {
            try {
				if(!App.client.getOurUser().getDisplayName(guild).equals("SwagBot"))
                	guild.setUserNickname(App.client.getOurUser(), "SwagBot");
            } catch (MissingPermissionsException | DiscordException e) {
                e.printStackTrace();
            }
        });
        App.setVolume(App.guilds.getGuildSettings(guild).getVolume(), guild);

		try {
			if (App.client.getVoiceChannelByID(lastChannel) != null && !lastChannel.equals(""))
				App.client.getVoiceChannelByID(lastChannel).join();
		} catch (MissingPermissionsException e) {
			e.printStackTrace();
		}

		if (queue.size() > 0) {
            AudioSource source;
            for (String url : queue) {
				try {
					if (url.contains("youtube"))
						source = new YouTubeAudio(url);
					else if (url.contains("soundcloud"))
						source = new SoundCloudAudio(url);
					else
						source = new AudioStream(url);
					getAudioPlayer().queue(source.getAudioTrack(null, false));
				} catch (NotStreamableException | YouTubeAPIException | UnirestException | YouTubeDLException | IOException | FFMPEGException | UnsupportedAudioFileException e) {
					e.printStackTrace();
				}
			}
        }
        return this;
    }

    public AudioPlayer getAudioPlayer() {
        return AudioPlayer.getAudioPlayerForGuild(guild);
    }
}