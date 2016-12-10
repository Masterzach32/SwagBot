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

import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.JsonNode
import com.mashape.unirest.http.Unirest
import net.masterzach32.commands4j.Command
import net.masterzach32.commands4j.MetadataMessageBuilder
import net.masterzach32.commands4j.Permission
import net.masterzach32.swagbot.utils.Utils
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.handle.obj.IUser
import java.net.URLEncoder

class TrumpQuoteCommand : Command("Donald Trump Quotes", "trumpquote", "donald", "trump") {

    override fun execute(cmdUsed: String, args: Array<String>, user: IUser, message: IMessage, channel: IChannel, permission: Permission): MetadataMessageBuilder? {
        val builder = MetadataMessageBuilder(channel)
        val response: HttpResponse<JsonNode>
        val base = "https://api.whatdoestrumpthink.com/api/"
        if (args.isEmpty())
            response = Unirest.get("${base}v1/quotes/random").asJson()
        else
            response = Unirest.get("${base}v1/quotes/personalized?q=${URLEncoder.encode(Utils.getContent(args, 0), "UTF-8")}").asJson()
        return builder.withContent("*${response.body.`object`.getString("message")}*\n\t-**Donald J. Trump**")
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("", "Gives you a random quote by Donald Trump.")
    }
}
