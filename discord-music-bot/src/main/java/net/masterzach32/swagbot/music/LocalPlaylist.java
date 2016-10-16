package net.masterzach32.swagbot.music;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.sound.sampled.UnsupportedAudioFileException;

import com.mashape.unirest.http.exceptions.UnirestException;
import net.masterzach32.swagbot.App;
import net.masterzach32.swagbot.music.player.*;
import net.masterzach32.swagbot.utils.exceptions.NotStreamableException;
import net.masterzach32.swagbot.utils.exceptions.YouTubeAPIException;
import org.json.JSONObject;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

public class LocalPlaylist {
	
	private String name;
	private boolean locked, requiresPerms;
	private List<AudioSource> music;

	public LocalPlaylist(String name, boolean locked, boolean requiresPerms) {
		this.name = name;
		this.locked = locked;
		this.requiresPerms = requiresPerms;
		music = new ArrayList<>();
	}

	public LocalPlaylist(String name, List<YouTubeAudio> music, boolean locked, boolean requiresPerms) throws UnirestException {
		this.name = name;
		this.locked = locked;
		this.requiresPerms = requiresPerms;
		this.music = new ArrayList<>();
        for(int i = 0; i < music.size(); i++)
            this.music.add(music.get(i));
	}

	protected LocalPlaylist(JSONObject json) {
        this.name = json.getString("name");
        this.locked = json.getBoolean("locked");
        this.requiresPerms = json.getBoolean("requiresPerms");
        this.music = new ArrayList<>();
        for(int i = 0; i < json.getJSONArray("music").length(); i++) {
			if(json.getJSONArray("music").get(i) instanceof JSONObject) {
                JSONObject jsonSource = (JSONObject) json.getJSONArray("music").get(i);
                AudioSource source;
                try {
                    if (jsonSource.getString("source").equals("youtube"))
                        source = new YouTubeAudio(jsonSource.getString("url"));
                    else if (jsonSource.getString("source").equals("soundcloud"))
                        source = new SoundCloudAudio(jsonSource.getString("url"));
                    else
                        source = new AudioStream(jsonSource.getString("url"));
                    music.add(source);
                } catch (NotStreamableException | UnirestException | YouTubeAPIException e) {
                    e.printStackTrace();
                }
            } else {
                String url = (String) json.getJSONArray("music").get(i);
                AudioSource source;
                try {
                    if (url.contains("youtube"))
                        source = new YouTubeAudio(url);
                    else if (url.contains("soundcloud"))
                        source = new SoundCloudAudio(url);
                    else
                        source = new AudioStream(url);
                    music.add(source);
                } catch (NotStreamableException | UnirestException | YouTubeAPIException e) {
                    e.printStackTrace();
                }
            }
        }
    }
	
	public AudioSource add(String audio) {
		for(int i = 0; i < music.size(); i++)
		    if(music.get(i).getUrl().equals(audio))
                return music.get(i);
        AudioSource source;
        try {
            if(audio.contains("youtube"))
                source = new YouTubeAudio(audio);
            else if(audio.contains("soundcloud"))
                source = new SoundCloudAudio(audio);
            else
                source = new AudioStream(audio);
            music.add(source);
            return source;
        } catch (NotStreamableException | UnirestException | YouTubeAPIException e) {
            e.printStackTrace();
        }
        return null;
	}
	
	public void remove(String audio) {
		for(int i = 0; i < music.size(); i++)
			if(music.get(i).getUrl().equals(audio))
				music.remove(i);
	}
	
	public String getName() {
		return name;
	}
	
	public void queue(IUser user, IGuild guild) {
		Collections.shuffle(music);
		for(AudioSource s : music) {
            try {
                App.playAudioFromAudioSource(s, true, null, user, guild);
            } catch (IOException | UnsupportedAudioFileException e) {
                e.printStackTrace();
            }
		}
	}
	
	public String getInfo() {
		String str = "";
		for(int i = 0; i < music.size(); i++) {
            AudioSource source = music.get(i);
			str += "" + (i + 1) + ". **" + source.getTitle() + "**\n";
		}
		return str;
	}

	public List<AudioSource> getSources() {
        return music;
    }
	
	public int songs() {
		return music.size();
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