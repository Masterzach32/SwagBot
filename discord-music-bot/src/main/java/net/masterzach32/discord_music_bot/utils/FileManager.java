package net.masterzach32.discord_music_bot.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileManager {
	
	public static final Logger logger = LoggerFactory.getLogger(FileManager.class);
	public List<File> files;

	public FileManager() {
		files = new ArrayList<File>();
		setup();
	}
	
	public File getFile(String fileName) {
		logger.debug("fetching:" + fileName);
		refresh();
		for(File file : files)
			if(file.getName().indexOf('.') >= 0 && file.getName().substring(0, file.getName().indexOf('.')).equals(fileName) || file.getName().contains(fileName))
				return file;
		return new File(fileName);
	}
	
	private void setup() {
		logger.debug("preparing:filesystem");
		files.add(new File(Constants.BINARY_STORAGE));
		files.add(new File(Constants.DIRECTORY_STORAGE));
		files.add(new File(Constants.AUDIO_CACHE));
		files.add(new File(Constants.PLAYLIST_CACHE));
		files.add(new File(Constants.LOG_STORAGE));
		files.add(new File(Constants.TEMP_STORAGE));
		for(File file : files) {
			if(!file.exists()) {
				if(file.getName().equals(Constants.BINARY_STORAGE)) {
					logger.error("FATAL ERROR: ffmpeg ffprobe youtube-dl NOT FOUND");
					System.exit(1);
				}
				file.mkdir();
				logger.debug("created:" + file.getName());
			} else {
				logger.debug("found:" + file.getName());
			}
		}
		refresh();
	}
	
	private void refresh() {
		files.clear();
		for(File file : new File("./").listFiles()) {
			files.add(file);
			if(file.isDirectory())
				logger.debug("refreshing:" + file.getName());
		}
		logger.debug("refresh:complete");
	}
}