package net.masterzach32.swagbot.api;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class R8Ball {
	
	private String response;

	public R8Ball() {
		try {
			response = Unirest.get("https://apis.rtainc.co/twitchbot/8ball").asString().getBody();
		} catch (UnirestException e) {
			e.printStackTrace();
		}
	}
	
	public String getResponse() {
		return response;
	}
}