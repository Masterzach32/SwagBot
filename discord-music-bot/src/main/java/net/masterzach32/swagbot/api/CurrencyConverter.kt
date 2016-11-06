/*
    SwagBot - A Discord Music Bot
    Copyright (C) 2016  Zachary Kozar

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc.,
    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package net.masterzach32.swagbot.api

import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.JsonNode
import com.mashape.unirest.http.Unirest
import com.mashape.unirest.http.exceptions.UnirestException
import net.masterzach32.swagbot.App
import org.json.JSONArray
import org.json.JSONObject

import java.util.ArrayList

class CurrencyConverter(from: String, to: String, amount: Double) {

    var fromCurrency: String? = null
        private set
    var toCurrency: String? = null
        private set
    var fromValue: Double = 0.toDouble()
        private set
    var toValue: Double = 0.toDouble()
        private set

    init {
        if (amount <= 0)
            throw IllegalArgumentException("Currency amount must be larger than 0")
        else {
            try {
                val response = Unirest.get("https://currencyconverter.p.mashape.com/?from=$from&from_amount=$amount&to=$to").header("X-Mashape-Key", "lmpj8JlDYfmshQLcaLKPJmpsn3g2p179SQojsnSWGVDlYuPMx8").header("Accept", "application/json").asJson()
                if (response.status != 200)
                    App.logger.info("CurrencyConverter API returned status code " + response.status)
                else {
                    val json = response.body.`object`
                    this.fromCurrency = json.getString("from")
                    this.toCurrency = json.getString("to")
                    fromValue = json.getDouble("from_amount")
                    toValue = json.getDouble("to_amount")
                }
            } catch (e: UnirestException) {
                e.printStackTrace()
            }

        }
    }

    companion object {
        val availableCurrencies: List<String>
            get() {
                val currencies = ArrayList<String>()
                try {
                    val response = Unirest.get("https://currencyconverter.p.mashape.com/availablecurrencies").header("X-Mashape-Key", "lmpj8JlDYfmshQLcaLKPJmpsn3g2p179SQojsnSWGVDlYuPMx8").header("Accept", "application/json").asJson()
                    if (response.status != 200)
                        App.logger.info("CurrencyConverter API returned status code " + response.status)
                    else {
                        val array = response.body.array
                        for (`object` in array) {
                            val json = `object` as JSONObject
                            currencies.add(json.getString("description"))
                        }
                    }
                } catch (e: UnirestException) {
                    e.printStackTrace()
                }
                return currencies
            }
    }
}