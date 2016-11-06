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

import com.mashape.unirest.http.Unirest
import com.mashape.unirest.http.exceptions.UnirestException
import net.masterzach32.swagbot.App

class RandomQuote(cat: String) {

    var quote: String? = null
        private set
    var author: String? = null
        private set
    var category: String? = null
        private set

    init {
        try {
            val response = Unirest.post("https://andruxnet-random-famous-quotes.p.mashape.com/?cat=" + cat).header("X-Mashape-Key", App.prefs.mashapApiKey).header("Content-Type", "application/x-www-form-urlencoded").header("Accept", "application/json").asJson()
            val json = response.body.array.getJSONObject(0)
            quote = json.getString("quote")
            author = json.getString("author")
            category = json.getString("category")
        } catch (e: UnirestException) {
            e.printStackTrace()
        }

    }
}