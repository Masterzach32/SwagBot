package net.masterzach32.discord_music_bot.api;

import org.json.JSONObject;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import net.masterzach32.discord_music_bot.App;
import net.masterzach32.discord_music_bot.EventHandler;
import sx.blah.discord.handle.obj.IGuild;

public class NSFWFilter {
	
	private double raw, partial, safe;
	private String url, status;

	public NSFWFilter(IGuild guild, String url) {
		String api_user = App.prefs.getAPIUser();
		String api_secret = App.prefs.getAPISecret();
		try {
			HttpResponse<JsonNode> response = Unirest.get("https://api.sightengine.com/1.0/nudity.json?api_user=" + api_user + "&api_secret=" + api_secret + "&url=" + url).asJson();
			JSONObject result = response.getBody().getArray().getJSONObject(0);
			if(result.getString("status").equals("failure"))
				EventHandler.logger.info(result.toString());
			else {
				JSONObject nudity = result.getJSONObject("nudity");
				status = result.getString("status");
				raw = nudity.getDouble("raw");
				partial = nudity.getDouble("partial");
				safe = nudity.getDouble("safe");
				this.url = url;
			}
			
		} catch (UnirestException e) {
			e.printStackTrace();
		}
	}
	
	public String getStatus() {
		return status;
	}
	
	public String getUrl() {
		return url;
	}
	
	public boolean isPartial() {
		return partial > .5;
	}
	
	public boolean isNSFW() {
		return raw > .5;
	}
	
	public boolean isSafe() {
		return safe > .5;
	}

	public double getRaw() {
		return raw*100;
	}

	public double getPartial() {
		return partial*100;
	}

	public double getSafe() {
		return safe*100;
	}
}