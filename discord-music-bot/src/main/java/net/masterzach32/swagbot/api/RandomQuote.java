package net.masterzach32.swagbot.api;

import org.json.JSONObject;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import net.masterzach32.swagbot.App;

public class RandomQuote {
	
	private String quote, author, category;

	public RandomQuote(String cat) {
		try {
			HttpResponse<JsonNode> response = Unirest.post("https://andruxnet-random-famous-quotes.p.mashape.com/?cat=" + cat)
				.header("X-Mashape-Key", App.prefs.getMashapApiKey())
				.header("Content-Type", "application/x-www-form-urlencoded")
				.header("Accept", "application/json")
				.asJson();
			JSONObject json = response.getBody().getArray().getJSONObject(0);
			quote = json.getString("quote");
			author = json.getString("author");
			category = json.getString("category");
		} catch (UnirestException e) {
			e.printStackTrace();
		}
	}
	
	public String getQuote() {
		return quote;
	}
	
	public String getAuthor() {
		return author;
	}
	
	public String getCategory() {
		return category;
	}
}