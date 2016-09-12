package net.masterzach32.discord_music_bot.api;

import org.json.JSONObject;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import net.masterzach32.discord_music_bot.App;

public class UrbanDefinition {
	
	private int defid;
	private String term, author, definition, example, link;

	public UrbanDefinition(String term) {
		try {
			HttpResponse<JsonNode> response = Unirest.get("https://mashape-community-urban-dictionary.p.mashape.com/define?term=" + term)
					.header("X-Mashape-Key", App.prefs.getMashapApiKey())
					.header("Accept", "text/plain")
					.asJson();
			JSONObject def = response.getBody().getArray().getJSONObject(0).getJSONArray("list").getJSONObject(0);
			this.defid = def.getInt("defid");
			this.term = def.getString("word");
			this.author = def.getString("author");
			this.definition = def.getString("definition");
			this.example = def.getString("example");
			this.link = def.getString("permalink");
		} catch (UnirestException e) {
			e.printStackTrace();
		}
	}
	
	public int getdefid() {
		return defid;
	}
	
	public String getTerm() {
		return term;
	}

	public String getAuthor() {
		return author;
	}

	public String getDefinition() {
		return definition;
	}

	public String getExample() {
		return example;
	}

	public String getLink() {
		return link;
	}
}