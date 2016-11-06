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
package net.masterzach32.swagbot.music.player;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.sound.sampled.UnsupportedAudioFileException;

import sx.blah.discord.handle.audio.IAudioProvider;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.audio.AudioPlayer.Track;
import sx.blah.discord.util.audio.providers.FileProvider;
import sx.blah.discord.util.audio.providers.URLProvider;

public class AudioTrack extends Track {
	
	private boolean announce;
	private String title;
	private String url;
	private IUser user;

	public AudioTrack(File file, String url, boolean announce, String title, IUser user) throws IOException, UnsupportedAudioFileException {
		super(new FileProvider(file));
		this.announce = announce;
		this.title = title;
		this.user = user;
        this.url = url;
	}

	public AudioTrack(String stream, boolean announce, String title, IUser user) throws IOException, UnsupportedAudioFileException {
		super(new URLProvider(new URL(stream)));
		this.url = stream;
		this.announce = announce;
		this.title = title;
		this.user = user;
	}

	public AudioTrack(IAudioProvider provider, String url, boolean announce, String title, IUser user) throws IOException, UnsupportedAudioFileException {
        super(provider);
        this.url = url;
        this.announce = announce;
        this.title = title;
        this.user = user;
    }
	
	public String getTitle() {
		return title;
	}
	
	public IUser getUser() {
		return user;
	}
	
	public boolean shouldAnnounce() {
		return announce;
	}

	public String getUrl() {
        return url;
    }
}