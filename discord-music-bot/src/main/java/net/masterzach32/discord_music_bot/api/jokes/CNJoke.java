package net.masterzach32.discord_music_bot.api.jokes;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class CNJoke implements IRandomJoke {
	
	private String joke;

	public CNJoke() {
		try {
			HttpResponse<JsonNode> json = Unirest.get("https://api.icndb.com/jokes/random").asJson();
			joke = json.getBody().getArray().getJSONObject(0).getJSONObject("value").getString("joke");
		} catch (UnirestException e) {
			e.printStackTrace();
		}
	}
	
	public String getJoke() {
		return joke;
	}
}