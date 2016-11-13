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
import net.masterzach32.swagbot.utils.Utils
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.handle.obj.IUser
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

class UrbanDictionaryCommand: Command("Urban Dictionary Lookup", "define") {

    override fun execute(cmdUsed: String, args: Array<String>, user: IUser, message: IMessage, channel: IChannel, permission: Permission): MetadataMessageBuilder? {
        val builder = MetadataMessageBuilder(channel)
        val def = UrbanDefinition(Utils.getContent(args, 0))
        if (def.hasEntry())
            return builder.withContent("${def.link}\n" +
                    "```" +
                    "Term: ${def.term}\n" +
                    "Definition: ${def.definition}\n" +
                    "Example: ${def.example}" +
                    "```")
        else
            return builder.withContent("Couldn't find a definition for **${def.term}**")
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("<term>", "Looks up a term on urban dictionary.")
    }

    class UrbanDefinition(term: String) {
        private var defid: Int = 0
        private var hasEntry: Boolean = false
        var term: String? = null
            private set
        var author: String? = null
            private set
        var definition: String? = null
            private set
        var example: String? = null
            private set
        var link: String? = null
            private set

        init {
            try {
                val response = Unirest.get("https://mashape-community-urban-dictionary.p.mashape.com/define?term=" + URLEncoder.encode(term, "UTF-8")).header("X-Mashape-Key", App.prefs.mashapApiKey).header("Accept", "text/plain").asJson()
                if (response.status != 200) {
                    App.logger.info("URLShortener api responded with status code " + response.status + ": " + response.statusText)
                }
                var def = response.body.array.getJSONObject(0)
                if (def.getJSONArray("list").length() == 0) {
                    hasEntry = false
                    this.term = term
                } else {
                    hasEntry = true
                    def = def.getJSONArray("list").getJSONObject(0)
                    this.defid = def.getInt("defid")
                    this.term = def.getString("word")
                    this.author = def.getString("author")
                    this.definition = def.getString("definition")
                    this.example = def.getString("example")
                    this.link = def.getString("permalink")
                }
            } catch (e: UnirestException) {
                e.printStackTrace()
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }

        }

        fun getdefid(): Int {
            return defid
        }

        fun hasEntry(): Boolean {
            return hasEntry
        }
    }
}