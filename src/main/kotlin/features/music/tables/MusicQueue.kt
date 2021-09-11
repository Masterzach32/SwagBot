package xyz.swagbot.features.music.tables

import discord4j.common.util.Snowflake
import io.facet.exposed.snowflake
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.Table
import xyz.swagbot.features.guilds.GuildTable

object MusicQueue : Table("music_queue") {

    val guildId = snowflake("guild_id") references GuildTable.guildId
    val identifier = text("identifier")
    val requesterId = snowflake("requester_id")
    val requestedChannelId = snowflake("requested_channel_id")

    fun whereGuildIs(guildId: Snowflake): SqlExpressionBuilder.() -> Op<Boolean> {
        return { this@MusicQueue.guildId eq guildId }
    }
}
