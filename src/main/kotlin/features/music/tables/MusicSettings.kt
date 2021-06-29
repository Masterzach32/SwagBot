package xyz.swagbot.features.music.tables

import discord4j.common.util.*
import io.facet.discord.exposed.*
import org.jetbrains.exposed.sql.*
import xyz.swagbot.features.guilds.*

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
