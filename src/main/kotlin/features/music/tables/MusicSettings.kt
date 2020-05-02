package xyz.swagbot.features.music.tables

import discord4j.core.`object`.util.*
import org.jetbrains.exposed.sql.*
import xyz.swagbot.features.guilds.*

object MusicSettings : Table("music_settings") {
    val guildId = long("guild_id") references GuildTable.guildId
    val enabled = bool("enabled").default(false)
    val volume = integer("volume").default(50)
    val currentlyConnectedChannel = long("channel_id").nullable()
    val loop = bool("loop").default(false)
    val autoplay = bool("autoplay").default(false)

    override val primaryKey = PrimaryKey(guildId)

    fun whereGuildIs(guildId: Snowflake): SqlExpressionBuilder.()->Op<Boolean> {
        return { this@MusicSettings.guildId eq guildId.asLong() }
    }
}
