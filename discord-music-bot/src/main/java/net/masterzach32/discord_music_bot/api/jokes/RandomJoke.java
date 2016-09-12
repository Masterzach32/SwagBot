package net.masterzach32.discord_music_bot.api.jokes;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

@Deprecated
public class RandomJoke implements IRandomJoke {

	private String joke;

	public RandomJoke() {
		try {
			HttpResponse<JsonNode> json = Unirest.get("http://webknox.com/api/jokes/random?apiKey=behdgeagjjawmcuumvxqlwjbsrvyjiv").asJson();
			joke = json.getBody().getArray().getJSONObject(0).getString("joke");
		} catch (UnirestException e) {
			e.printStackTrace();
		}
	}
	
	public String getJoke() {
		return joke;
	}
}