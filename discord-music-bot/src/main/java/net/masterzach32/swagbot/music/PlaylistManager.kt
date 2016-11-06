package net.masterzach32.swagbot.music

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.RandomAccessFile
import java.net.URLEncoder
import java.util.ArrayList

import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.google.gson.GsonBuilder
import net.masterzach32.swagbot.App

import net.masterzach32.swagbot.utils.Constants

class PlaylistManager(private val guildID: String) {

    private val playlists: MutableList<LocalPlaylist>

    init {
        playlists = ArrayList<LocalPlaylist>()
    }

    fun save() {
        for (p in playlists) {
            val fout: BufferedWriter
            try {
                fout = BufferedWriter(FileWriter(Constants.GUILD_SETTINGS + guildID + "/playlists/" + URLEncoder.encode(p.name!!, "UTF-8") + ".json"))
                fout.write(GsonBuilder().setPrettyPrinting().create().toJson(p))
                fout.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    fun load() {
        this.playlists.clear()
        val playlists = App.manager.getFile(Constants.GUILD_SETTINGS + guildID + "/playlists/").listFiles()
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

    fun remove(name: String) {
        for (i in playlists.indices)
            if (playlists[i].name!!.toLowerCase() == name.toLowerCase())
                playlists.removeAt(i)
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