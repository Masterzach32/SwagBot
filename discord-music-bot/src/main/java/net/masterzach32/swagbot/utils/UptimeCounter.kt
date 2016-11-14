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
package net.masterzach32.swagbot.utils

class UptimeCounter : Thread("Uptime Counter") {

    var uptime: Long = 0

    init {
        start()
    }

    override fun run() {
        while (true) {
            uptime++
            Thread.sleep(1000)
        }
    }

    override fun toString(): String {
        return "${uptime/3600}:${uptime/60%60}:${uptime%60}"
    }
}