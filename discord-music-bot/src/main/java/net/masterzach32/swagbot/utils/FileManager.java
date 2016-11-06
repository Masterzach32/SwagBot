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
package net.masterzach32.swagbot.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileManager {

	public static final Logger logger = LoggerFactory.getLogger(FileManager.class);
	public List<File> files;

	public FileManager() {
		files = new ArrayList<>();
		setup();
		File[] files = getFile(Constants.INSTANCE.getWORKING_DIRECTORY()).listFiles();
		Arrays.stream(files)
                .filter((file -> file.getName().toLowerCase().contains("frag")))
                .collect(Collectors.toList())
                .forEach(File::delete);
	}

	public synchronized File getFile(String fileName) {
		logger.info("fetching:" + fileName);
		refresh();
		for(File file : files)
			if(file.getName().indexOf('.') >= 0 && file.getName().substring(0, file.getName().indexOf('.')).equals(fileName) || file.getName().contains(fileName))
				return file;
		return new File(fileName);
	}

	public synchronized void mkdir(String dir) {
		File file = new File(dir);
		if(!file.exists()) {
			file.mkdirs();
		}
	}

	private void setup() {
		logger.info("preparing:filesystem");
		files.add(new File(Constants.INSTANCE.getBINARY_STORAGE()));
		files.add(new File(Constants.INSTANCE.getDIRECTORY_STORAGE()));
		files.add(new File(Constants.INSTANCE.getAUDIO_CACHE()));
		files.add(new File(Constants.INSTANCE.getGUILD_SETTINGS()));
		files.add(new File(Constants.INSTANCE.getLOG_STORAGE()));
		files.add(new File(Constants.INSTANCE.getTEMP_STORAGE()));
		for(File file : files) {
			if(!file.exists()) {
				file.mkdir();
				logger.info("created:" + file.getName());
			} else {
				logger.info("found:" + file.getName());
			}
		}
		refresh();
	}

	private void refresh(File dir) {
		for(File file : dir.listFiles()) {
			files.add(file);
			if(file.isDirectory()) {
				//logger.info("refreshing:" + file.getTitle());
				refresh(file);
			}
		}
	}

	private synchronized void refresh() {
		files.clear();
		refresh(new File(Constants.INSTANCE.getWORKING_DIRECTORY()));
		//logger.info("refresh:complete");
	}
}