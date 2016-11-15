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
package net.masterzach32.swagbot.commands.normal

import com.mashape.unirest.http.Unirest
import net.masterzach32.commands4j.*
import net.masterzach32.commands4j.MetadataMessageBuilder
import net.masterzach32.swagbot.utils.Utils
import org.json.JSONArray
import org.json.JSONObject
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.handle.obj.IUser
import java.util.*

class StrawpollCommand: Command("Create Strawpoll", "strawpoll") {

    override fun execute(cmdUsed: String, args: Array<String>, user: IUser, message: IMessage, channel: IChannel, permission: Permission): MetadataMessageBuilder? {
        val choices = Utils.delimitWithoutEmpty(Utils.getContent(args, 0), "\\|")
        if (choices.size < 3 || choices.size > 31)
            return getWrongArgumentsMessage(channel, this, cmdUsed)
        choices.forEach { it.trim().replace("\n", "") }

        val body = JSONObject()
        body.put("title", choices[0])
        body.put("options", JSONArray(Arrays.copyOfRange(choices, 1, choices.size)))
        body.put("dupcheck", "normal")
        body.put("multi", false)
        body.put("captcha", true)

        val pollUrl = "http://www.strawpoll.me/api/v2/polls"
        val response = Unirest.post(pollUrl)
                .body(body.toString())
                .asJson()

        if (response.status != 200)
            return getApiErrorMessage(channel, Type.POST, pollUrl, body.toString(2), response.status, response.statusText)

        val json = response.body.`object`
        val id = json.getInt("id")
        return MetadataMessageBuilder(channel).withContent("__Strawpoll created by **${user.getDisplayName(message.guild)}**__\n")
                .appendContent("Title: **${choices[0]}**\n")
                .appendContent("Strawpoll link: <https://strawpoll.me/$id>")
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("<title> | <option 1> | <option 2> [| [option 3]]", "Create a strawpoll with the title and options. You must have at least two options and no more than 30.")
    }
}