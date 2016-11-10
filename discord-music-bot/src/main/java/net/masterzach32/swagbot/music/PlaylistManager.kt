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

import java.io.BufferedWriter
import java.io.FileWriter
import java.io.IOException
import java.io.RandomAccessFile
import java.net.URLEncoder
import java.util.ArrayList

import org.json.JSONObject
import org.slf4j.LoggerFactory

import com.google.gson.GsonBuilder
import net.masterzach32.swagbot.App

import net.masterzach32.swagbot.utils.GUILD_SETTINGS

class PlaylistManager(private val guildID: String) {

    private val playlists: MutableList<LocalPlaylist>

    init {
        playlists = ArrayList<LocalPlaylist>()
    }

    fun save() {
        for (p in playlists) {
            val fout: BufferedWriter
            try {
                fout = BufferedWriter(FileWriter("$GUILD_SETTINGS$guildID/playlists/" + URLEncoder.encode(p.name!!, "UTF-8") + ".json"))
                fout.write(GsonBuilder().setPrettyPrinting().create().toJson(p))
                fout.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    fun load() {
        this.playlists.clear()
        val playlists = App.manager.getFile("$GUILD_SETTINGS$guildID/playlists/").listFiles()
        for (file in playlists!!) {
            val fin: RandomAccessFile
            var buffer: ByteArray? = null

            try {
                fin = RandomAccessFile(file, "r")
                buffer = ByteArray(fin.length().toInt())
                fin.readFully(buffer)
                fin.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            val json = String(buffer!!)
            val obj = JSONObject(json)
            val p = LocalPlaylist(obj)
            this.playlists.add(p)
            logger.info("loaded:" + file.name)
        }
    }

    fun add(p: LocalPlaylist) {
        playlists.add(p)
    }

    fun remove(name: String): Boolean {
        playlists
                .filter { it.name!!.toLowerCase() == name.toLowerCase() }
                .forEach { return playlists.remove(it) }
        return false
    }

    operator fun get(name: String): LocalPlaylist? {
        for (i in playlists.indices)
            if (playlists[i].name!!.toLowerCase() == name.toLowerCase())
                return playlists[i]
        return null
    }

    override fun toString(): String {
        var str = ""
        for (p in playlists)
            str += p.name + ":" + p.songs() + " "
        return str
    }

    companion object {

        val logger = LoggerFactory.getLogger(App::class.java)
    }
}