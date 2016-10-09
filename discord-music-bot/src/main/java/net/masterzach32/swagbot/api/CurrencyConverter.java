package net.masterzach32.swagbot.api;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.masterzach32.swagbot.App;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CurrencyConverter {

    private String from, to;
    private double fromValue, toValue;

    public CurrencyConverter(String from, String to, double amount) {
        if(amount <= 0)
            throw new IllegalArgumentException("Currency amount must be larger than 0");
        else {
            try {
                HttpResponse<JsonNode> response = Unirest.get("https://currencyconverter.p.mashape.com/?from=" + from + "&from_amount=" + amount + "&to=" + to)
                        .header("X-Mashape-Key", "lmpj8JlDYfmshQLcaLKPJmpsn3g2p179SQojsnSWGVDlYuPMx8")
                        .header("Accept", "application/json")
                        .asJson();
                if(response.getStatus() != 200)
                    App.logger.info("CurrencyConverter API returned status code " + response.getStatus());
                else {
                    JSONObject json = response.getBody().getObject();
                    this.from = json.getString("from");
                    this.to = json.getString("to");
                    fromValue = json.getDouble("from_value");
                    toValue = json.getDouble("to_value");
                }
            } catch (UnirestException e) {
                e.printStackTrace();
            }
        }
    }

    public String getFromCurrency() {
        return from;
    }

    public String getToCurrency() {
        return to;
    }

    public double getFromValue() {
        return fromValue;
    }

    public double getToValue() {
        return toValue;
    }

    public static List<String> getAvailableCurrencies() {
        List<String> currencies = new ArrayList<>();
        try {
            HttpResponse<JsonNode> response = Unirest.get("https://currencyconverter.p.mashape.com/availablecurrencies")
                    .header("X-Mashape-Key", "lmpj8JlDYfmshQLcaLKPJmpsn3g2p179SQojsnSWGVDlYuPMx8")
                    .header("Accept", "application/json")
                    .asJson();
            if(response.getStatus() != 200) {
                JSONArray array = response.getBody().getArray();
                for(Object object : array) {
                    JSONObject json = (JSONObject) object;
                    currencies.add(json.getString("description"));
                }
            }
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        return currencies;
    }
}