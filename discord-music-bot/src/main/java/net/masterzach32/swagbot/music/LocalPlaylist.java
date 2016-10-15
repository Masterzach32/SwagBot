package net.masterzach32.swagbot.music;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sound.sampled.UnsupportedAudioFileException;

import com.mashape.unirest.http.exceptions.UnirestException;
import net.masterzach32.swagbot.App;
import net.masterzach32.swagbot.music.player.*;
import net.masterzach32.swagbot.utils.exceptions.NotStreamableException;
import org.json.JSONObject;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

public class LocalPlaylist {
	
	private String name;
	private boolean locked, requiresPerms;
	private List<AudioSource> sources;

	public LocalPlaylist(String name, boolean locked, boolean requiresPerms) {
		this.name = name;
		this.locked = locked;
		this.requiresPerms = requiresPerms;
		sources = new ArrayList<>();
	}

	public LocalPlaylist(String name, List<YouTubeAudio> music, boolean locked, boolean requiresPerms) throws UnirestException {
		this.name = name;
		this.locked = locked;
		this.requiresPerms = requiresPerms;
		this.sources = new ArrayList<>();
        for(int i = 0; i < music.size(); i++)
            this.sources.add(music.get(i));
	}

	protected LocalPlaylist(JSONObject json) {
        this.name = json.getString("name");
        this.locked = json.getBoolean("locked");
        this.requiresPerms = json.getBoolean("requiresPerms");
        this.sources = new ArrayList<>();
        for(int i = 0; i < json.getJSONArray("sources").length(); i++) {
            JSONObject jsonSource = (JSONObject) json.getJSONArray("sources").get(i);
            AudioSource source;
            try {
                if (jsonSource.getString("source").equals("youtube"))
                    source = new YouTubeAudio(jsonSource.getString("url"));
                else if (jsonSource.getString("source").equals("soundcloud"))
                    source = new SoundCloudAudio(jsonSource.getString("url"));
                else
                    source = new AudioStream(jsonSource.getString("url"));
                sources.add(source);
            } catch (NotStreamableException | UnirestException e) {
                e.printStackTrace();
            }
        }
    }
	
	public AudioSource add(String audio) {
		for(int i = 0; i < sources.size(); i++)
		    if(sources.get(i).getUrl().equals(audio))
                return sources.get(i);
        AudioSource source;
        try {
            if(audio.contains("youtube"))
                source = new YouTubeAudio(audio);
            else if(audio.contains("soundcloud"))
                source = new SoundCloudAudio(audio);
            else
                source = new AudioStream(audio);
            sources.add(source);
            return source;
        } catch (NotStreamableException | UnirestException e) {
            e.printStackTrace();
        }
        return null;
	}
	
	public void remove(String audio) {
		for(int i = 0; i < sources.size(); i++)
			if(sources.get(i).getUrl().equals(audio))
				sources.remove(i);
	}
	
	public String getName() {
		return name;
	}
	
	public void queue(IUser user, IGuild guild) {
		Collections.shuffle(sources);
		ExecutorService executor = Executors.newFixedThreadPool(2);
		for(AudioSource s : sources) {
			Thread task = new Thread("loadAudioFromPlaylist:" + s) {
				public void run() {
					try {
						App.playAudioFromAudioSource(s, true, null, user, guild);
					} catch (IOException | UnsupportedAudioFileException e) {
						e.printStackTrace();
					}
				}
			};
			executor.execute(task);
		}
	}
	
	public String getInfo() {
		String str = "";
		for(int i = 0; i < sources.size(); i++) {
            AudioSource source = sources.get(i);
			str += "" + (i + 1) + ". **" + source.getTitle() + "**\n";
		}
		return str;
	}

	public List<AudioSource> getSources() {
        return sources;
    }
	
	public int songs() {
		return sources.size();
	}
	
	public boolean isLocked() {
		return locked;
	}
	
	public void toggleLocked() {
		this.locked = !locked;
	}
	
	public boolean requiresPerms() {
		return requiresPerms;
	}
	
	public void toggleRequiresPerms() {
		this.requiresPerms = !requiresPerms;
	}
}