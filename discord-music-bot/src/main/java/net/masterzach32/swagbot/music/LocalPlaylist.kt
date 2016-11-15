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
package net.masterzach32.swagbot.music

import java.io.IOException
import java.util.ArrayList
import java.util.Collections

import javax.sound.sampled.UnsupportedAudioFileException

import com.mashape.unirest.http.exceptions.UnirestException
import net.masterzach32.swagbot.App
import net.masterzach32.swagbot.guilds.GuildSettings
import net.masterzach32.swagbot.music.player.*
import net.masterzach32.swagbot.utils.exceptions.NotStreamableException
import net.masterzach32.swagbot.utils.exceptions.YouTubeAPIException
import org.json.JSONObject
import sx.blah.discord.handle.obj.IGuild
import sx.blah.discord.handle.obj.IUser

class LocalPlaylist {

    val name: String
    var isLocked: Boolean = false
        private set
    private var requiresPerms: Boolean = false
    private val music: MutableList<AudioSource>

    constructor(name: String, locked: Boolean, requiresPerms: Boolean) {
        this.name = name
        this.isLocked = locked
        this.requiresPerms = requiresPerms
        music = ArrayList<AudioSource>()
    }

    @Throws(UnirestException::class)
    constructor(name: String, music: List<AudioSource>, locked: Boolean, requiresPerms: Boolean) {
        this.name = name
        this.isLocked = locked
        this.requiresPerms = requiresPerms
        this.music = ArrayList<AudioSource>()
        music.forEach { this.music.add(it) }
    }

    constructor(json: JSONObject) {
        this.name = json.getString("name")
        this.isLocked = json.getBoolean("isLocked")
        this.requiresPerms = json.getBoolean("requiresPerms")
        this.music = ArrayList<AudioSource>()
        for (i in 0..json.getJSONArray("music").length() - 1) {
            if (json.getJSONArray("music").get(i) is JSONObject) {
                val jsonSource = json.getJSONArray("music").get(i) as JSONObject
                val source: AudioSource
                try {
                    if (jsonSource.getString("source") == "youtube")
                        source = YouTubeAudio(jsonSource.getString("url"))
                    else if (jsonSource.getString("source") == "soundcloud")
                        source = SoundCloudAudio(jsonSource.getString("url"))
                    else
                        source = AudioStream(jsonSource.getString("url"))
                    music.add(source)
                } catch (e: NotStreamableException) {
                    e.printStackTrace()
                } catch (e: UnirestException) {
                    e.printStackTrace()
                } catch (e: YouTubeAPIException) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun add(audio: String): AudioSource? {
        music.indices
                .filter { music[it].url == audio }
                .forEach { return music[it] }
        val source: AudioSource
        try {
            if (audio.contains("youtube"))
                source = YouTubeAudio(audio)
            else if (audio.contains("soundcloud"))
                source = SoundCloudAudio(audio)
            else
                source = AudioStream(audio)
            music.add(source)
            return source
        } catch (e: NotStreamableException) {
            e.printStackTrace()
        } catch (e: UnirestException) {
            e.printStackTrace()
        } catch (e: YouTubeAPIException) {
            e.printStackTrace()
        }

        return null
    }

    fun remove(audio: String) {
        music.indices
                .filter { music[it].url == audio }
                .forEach { music.removeAt(it) }
    }

    fun queue(user: IUser, guild: GuildSettings) {
        Collections.shuffle(music)
        for (s in music) {
            try {
                guild.playAudioFromAudioSource(s, null, user)
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: UnsupportedAudioFileException) {
                e.printStackTrace()
            }

        }
    }

    fun getInfo(): String {
        var str = ""
        for (i in music.indices) {
            val source = music[i]
            str += "" + (i + 1) + ". **" + source.title + "**\n"
        }
        return str
    }

    fun getSources(): MutableList<AudioSource>? {
        return music
    }

    fun songs(): Int {
        return music.size
    }

    fun toggleLocked() {
        this.isLocked = !isLocked
    }

    fun requiresPerms(): Boolean {
        return requiresPerms
    }

    fun toggleRequiresPerms() {
        this.requiresPerms = !requiresPerms
    }
}