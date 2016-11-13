/*
    SwagBot-java
    Copyright (C) 2016 Zach Kozar

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
package net.masterzach32.swagbot.commands.`fun`

import com.mashape.unirest.http.Unirest
import com.mashape.unirest.http.exceptions.UnirestException
import net.masterzach32.commands4j.Command
import net.masterzach32.commands4j.Permission
import net.masterzach32.commands4j.util.MetadataMessageBuilder
import net.masterzach32.swagbot.App
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.handle.obj.IUser

class QuoteCommand: Command("Random Quote", "quote") {

    override fun execute(cmdUsed: String, args: Array<String>, user: IUser, message: IMessage, channel: IChannel, permission: Permission): MetadataMessageBuilder? {
        val response: RandomQuote
        if ((Math.random() * 2).toInt() == 1)
            response = RandomQuote("movies")
        else
            response = RandomQuote("famous")
        return MetadataMessageBuilder(channel).withContent("*\"${response.quote}\"*\n\t-**${response.author}**")
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("", "Gives you a random quote.")
    }

    class RandomQuote(var category: String, var quote: String = "", var author: String = "") {
        init {
            val response = Unirest.post("https://andruxnet-random-famous-quotes.p.mashape.com/?cat=" + category)
                    .header("X-Mashape-Key", App.prefs.mashapApiKey)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Accept", "application/json").asJson()
            val json = response.body.array.getJSONObject(0)
            category = json.getString("category")
            quote = json.getString("quote")
            author = json.getString("author")
        }
    }
}