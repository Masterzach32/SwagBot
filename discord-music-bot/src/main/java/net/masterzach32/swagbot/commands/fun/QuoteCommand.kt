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
import net.masterzach32.commands4j.Type
import net.masterzach32.commands4j.getApiErrorMessage
import net.masterzach32.commands4j.MetadataMessageBuilder
import net.masterzach32.swagbot.App
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.handle.obj.IUser

class QuoteCommand: Command("Random Quote", "quote") {

    override fun execute(cmdUsed: String, args: Array<String>, user: IUser, message: IMessage, channel: IChannel, permission: Permission): MetadataMessageBuilder? {
        val category: String
        if ((Math.random() * 2).toInt() == 1)
            category = "movies"
        else
            category = "famous"
        val url = "https://andruxnet-random-famous-quotes.p.mashape.com/?cat=$category"

        val response = Unirest.post(url)
                .header("X-Mashape-Key", App.prefs.mashapApiKey)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "application/json").asJson()

        if (response.status != 200)
            return getApiErrorMessage(channel, Type.POST, url, "none", response.status, response.statusText)

        val json = response.body.`object`
        return MetadataMessageBuilder(channel).withContent("*\"${json.getString("quote")}\"*\n\t-**${json.getString("author")}**")
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("", "Gives you a random quote.")
    }
}