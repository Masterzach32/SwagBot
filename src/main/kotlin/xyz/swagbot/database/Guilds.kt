package xyz.swagbot.database

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import sx.blah.discord.handle.obj.IGuild

/*
 * SwagBot - Created on 8/24/17
 * Author: zachk
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at the project root.
 */

/**
 * @author zachk
 * @version 8/24/17
 */

fun IGuild.getCommandPrefix(): String {
    return getGuildRow(stringID)[sb_guilds.command_prefix]
}

fun IGuild.getBotVolume(): Int {
    return getGuildRow(stringID)[sb_guilds.volume]
}

fun IGuild.isBotLocked(): Boolean {
    return getGuildRow(stringID)[sb_guilds.locked]
}

private fun getGuildRow(id: String): ResultRow {
    return sql { return@sql sb_guilds.select { sb_guilds.id eq id }.first() }
}