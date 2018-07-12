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

object ApiKeys : Table("sb_api_keys") {
    val api_name = text("api_name").primaryKey()
    val api_key = text("api_key")
}

object Guilds : Table("sb_guilds") {
    val id = long("id").primaryKey()
    val name = text("name")
    val command_prefix = text("command_prefix")
    val locked = bool("locked").default(false)
    val volume = integer("volume").default(50)
    val loop = bool("loop").default(false)
    val auto_assign_role = text("auto_assign_role").nullable()
    val last_voice_channel = long("last_voice_channel").nullable()
    val timezone = text("timezone")
    val game_switcher = bool("game_switcher").default(false)
}

data class GuildSettingsLoadObj(val id: Long, val volume: Int, val loop: Boolean, val lastVoiceChannel: Long?)

object Permissions : Table("sb_permissions") {
    val guild_id = long("guild_id")
    val user_id = long("user_id")
    val permission = integer("permission")
}

object Stats : Table("sb_stats") {
    val key = text("key")
    val value = integer("value").default(0)
}

object ChatChannels : Table("sb_chat_channels") {
    val guild_id = long("guild_id")
    val channel_id = long("channel_id")
}

object BrawlQuotes : Table("sb_game_brawl") {
    val id = integer("id")
    val death_message = text("response")
}

object TrackStorage : Table("sb_track_storage") {
    val guild_id = long("guild_id")
    val user_id = long("user_id")
    val identifier = text("identifier")
}

object IAmRoles : Table("sb_iam_roles") {
    val role_id = long("role_id")
}

object MusicProfile : Table("sb_music_profile") {
    val user_id = long("user_id")
    val identifier = text("identifier")
    val count = integer("count")
}

object GameSwitcher : Table("sb_game_switcher") {
    val guild_id = long("guild_id")
    val game = text("game")
    val channel_id = long("channel_id")
}

object PollChannels : Table("sb_poll_channels") {
    val id = long("channel_id").primaryKey()
}