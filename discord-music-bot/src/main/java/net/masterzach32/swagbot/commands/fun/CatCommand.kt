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
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.handle.obj.IUser

class CatCommand: Command("Random Cat", "cat") {

    override fun execute(cmdUsed: String, args: Array<String>, user: IUser, message: IMessage, channel: IChannel, permission: Permission): MetadataMessageBuilder? {
        return MetadataMessageBuilder(channel).withContent(RandomCat().url)
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("", "Gives you a random cat picture.")
    }

    class RandomCat {
        val url: String
        init {
            url = try {
                Unirest.get("http://random.cat/meow").asJson().body.`object`.getString("file")
            } catch (e: UnirestException) {
                e.printStackTrace()
                "random.cat api error, please report this to the developer"
            }
        }
    }
}