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
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.handle.obj.IUser

class R8BallCommand: Command("8 Ball", "8ball", "8") {

    override fun execute(cmdUsed: String, args: Array<String>, user: IUser, message: IMessage, channel: IChannel, permission: Permission): MetadataMessageBuilder? {
        val url = "https://apis.rtainc.co/twitchbot/8ball"
        val response = Unirest.get(url).asString()

        if (response.status != 200)
            return getApiErrorMessage(channel, Type.GET, url, "none", response.status, response.statusText)
        return MetadataMessageBuilder(channel).withContent(response.body)
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("[question]", "Gives you a prediction to your question.")
    }
}