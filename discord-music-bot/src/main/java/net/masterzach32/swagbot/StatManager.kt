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
package net.masterzach32.swagbot

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import net.masterzach32.swagbot.utils.UptimeCounter
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.RandomAccessFile
import java.util.*

class StatManager(val map: HashMap<String, Any>) {

    fun update(obj: Any) {

    }

    fun save(storage: String) {
        val fout: BufferedWriter
        fout = BufferedWriter(FileWriter(storage))
        fout.write(GsonBuilder().setPrettyPrinting().create().toJson(map))
        fout.close()
    }

    fun reset() {
        map.clear()
    }
}

fun load(storage: String): StatManager {
    val stats: StatManager
    if(File(storage).exists()) {
        val fin: RandomAccessFile
        val buffer: ByteArray

        fin = RandomAccessFile(storage, "r")
        buffer = ByteArray(fin.length().toInt())
        fin.readFully(buffer)
        fin.close()

        val json = String(buffer)
        stats = StatManager(Gson().fromJson(json, HashMap::class.java) as HashMap<String, Any>)
    } else {
        stats = StatManager(HashMap<String, Any>())
        val map = stats.map
        map.put("Bot Version", "1.0")
        map.put("Uptime", UptimeCounter())
        map.put("Java Version", System.getProperty("java.version"))
        map.put("Server", "${System.getProperty("os.name")} ${System.getProperty("os.arch")}")
        map.put("Threads", Thread.activeCount())
        map.put("RAM", "0 MB / ${Runtime.getRuntime().totalMemory()/1000000} MB")
        map.put("Guilds", 0)
        map.put("Commands Received", 0)
    }
    return stats
}