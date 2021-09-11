package xyz.swagbot.features.music.tables

import discord4j.common.util.Snowflake
import io.facet.exposed.snowflake
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.Table
import xyz.swagbot.features.guilds.GuildTable

object MusicSettings : Table("music_settings") {

    val guildId = snowflake("guild_id") references GuildTable.guildId
    val enabled = bool("enabled").default(false)
    val volume = integer("volume").default(100)
    val lastConnectedChannel = snowflake("channel_id").nullable()
    val loop = bool("loop").default(false)
    val autoplay = bool("autoplay").default(false)

    override val primaryKey = PrimaryKey(guildId)

    fun whereGuildIs(guildId: Snowflake): SqlExpressionBuilder.() -> Op<Boolean> = {
        this@MusicSettings.guildId eq guildId
    }
}
