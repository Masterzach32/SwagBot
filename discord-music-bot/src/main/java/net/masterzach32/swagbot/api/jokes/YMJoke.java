package net.masterzach32.swagbot.api.jokes;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class YMJoke implements IRandomJoke {

	private String joke;

	public YMJoke() {
		try {
			HttpResponse<JsonNode> json = Unirest.get("http://api.yomomma.info/").asJson();
			joke = json.getBody().getArray().getJSONObject(0).getString("joke");
		} catch (UnirestException e) {
			e.printStackTrace();
		}
	}
	
	public String getJoke() {
		return joke;
	}
}