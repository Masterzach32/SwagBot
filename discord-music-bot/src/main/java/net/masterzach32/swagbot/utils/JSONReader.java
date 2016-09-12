package net.masterzach32.swagbot.utils;

import java.io.IOException;

public interface JSONReader {

	public void load() throws IOException;
	
	public void save() throws IOException;
	
}