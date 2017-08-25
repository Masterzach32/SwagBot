package xyz.swagbot.database

import org.jetbrains.exposed.sql.Table

/*
 * SwagBot - Created on 8/22/17
 * Author: Zach Kozar
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at the project root.
 */

/**
 * @author Zach Kozar
 * @version 8/22/17
 */

object sb_api_keys : Table() {
    val api_name = text("api_name")
    val api_key = text("api_key")
}

object sb_guilds : Table() {
    val id = text("id")
    val name = text("name")
    val command_prefix = text("command_prefix")
    val volume = integer("volume")
    val locked = bool("locked")
}