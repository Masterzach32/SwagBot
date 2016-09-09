package net.masterzach32.discord_music_bot.api;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class RandomCat {
	
	private String url;

	public RandomCat() {
		try {
			HttpResponse<JsonNode> json = Unirest.get("http://random.cat/meow").asJson();
			url = json.getBody().getArray().getJSONObject(0).getString("file");
		} catch (UnirestException e) {
			e.printStackTrace();
		}
	}
	
	public String getUrl() {
		return url;
	}
}