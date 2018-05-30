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

object sb_defaults : Table() {
    val key = text("key")
    val value = text("value")
}

object sb_guilds : Table() {
    val id = text("id")
    val name = text("name")
    val command_prefix = text("command_prefix").default(getDefault("command_prefix"))
    val locked = bool("locked").default(false)
    val volume = integer("volume").default(50)
    val loop = bool("loop").default(false)
    val auto_assign_role = text("auto_assign_role").nullable()
    val last_voice_channel = text("last_voice_channel").nullable()
    val timezone = text("timezone").default("EST")
    val game_switcher = bool("game_switcher").default(false)
}

object sb_permissions : Table() {
    val guild_id = text("guild_id")
    val user_id = text("user_id")
    val permission = integer("permission")
}

object sb_stats : Table() {
    val key = text("key")
    val value = integer("value")
}

object sb_chat_channels : Table() {
    val guild_id = text("guild_id")
    val channel_id = text("channel_id")
}

object sb_game_brawl : Table() {
    val id = integer("id")
    val death_message = text("response")
}

object sb_track_storage : Table() {
    val guild_id = text("guild_id")
    val user_id = text("user_id")
    val identifier = text("identifier")
}

object sb_iam_roles : Table() {
    val guild_id = text("guild_id")
    val role_id = text("role_id")
}

object sb_music_profile : Table() {
    val user_id = text("user_id")
    val identifier = text("identifier")
    val count = integer("count")
}

object sb_game_switcher : Table() {
    val guild_id = text("guild_id")
    val game = text("game")
    val channel_id = text("channel_id")
}