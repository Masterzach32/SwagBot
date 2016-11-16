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
import org.json.JSONObject

class URLShortener(url: String) {

    var url: String? = null
        private set

    init {
        try {
            val obj = JSONObject()
            obj.put("longUrl", url)
            val response = Unirest.post("https://www.googleapis.com/urlshortener/v1/url?key=" + App.prefs.googleAuthKey).header("Content-Type", "application/json").body(obj.toString()).asJson()
            if (response.status != 200)
                App.logger.info("Google API responded with status code " + response.status + ": " + response.statusText)
            else
                this.url = response.body.`object`.getString("id")
        } catch (e: UnirestException) {
            e.printStackTrace()
        }
    }
}