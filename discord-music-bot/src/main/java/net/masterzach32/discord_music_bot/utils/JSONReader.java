package net.masterzach32.discord_music_bot.utils;

import java.io.IOException;

public interface JSONReader {

	public void load() throws IOException;
	
	public void save() throws IOException;
	
}