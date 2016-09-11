package net.masterzach32.discord_music_bot.api;

import org.json.JSONObject;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class NSFWFilter {
	
	private String skinColorLevel, reason, url;
	private boolean isPorn, containsKeywords;

	public NSFWFilter(String url) {
		try {
			HttpResponse<JsonNode> response = Unirest.get("https://sphirelabs-advanced-porn-nudity-and-adult-content-detection.p.mashape.com/v1/get/index.php?url=" + url)
					.header("X-Mashape-Key", "lmpj8JlDYfmshQLcaLKPJmpsn3g2p179SQojsnSWGVDlYuPMx8")
					.header("Accept", "application/json")
					.asJson();
			JSONObject result = response.getBody().getArray().getJSONObject(0);
			
			skinColorLevel = result.getString("Skin Colors");
			reason = result.getString("Reason");
			isPorn = result.getBoolean("Is Porn");
			containsKeywords = result.getBoolean("Is Contain Bad Words");
			this.url = url;
			
		} catch (UnirestException e) {
			e.printStackTrace();
		}
	}
	
	public String getResult() {
		return "Your link `" + url + "` was removed for the folowing reason:\n**Skin Color Level: " + skinColorLevel + "**\n" + reason;
	}

	public String getSkinColorLevel() {
		return skinColorLevel;
	}

	public String getReason() {
		return reason;
	}

	public boolean isPorn() {
		return isPorn;
	}

	public boolean isContainsKeywords() {
		return containsKeywords;
	}
}