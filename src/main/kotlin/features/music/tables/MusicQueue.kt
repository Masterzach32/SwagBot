package xyz.swagbot.features.music.tables

import discord4j.core.`object`.util.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import xyz.swagbot.features.guilds.*

object MusicQueue : Table("music_queue") {

    val guildId = long("guild_id") references GuildTable.guildId
    val identifier = text("identifier")
    val requesterId = long("requester_id")
    val requestedChannelId = long("requested_channel_id")

    fun whereGuildIs(guildId: Snowflake): SqlExpressionBuilder.()->Op<Boolean> {
        return { this@MusicQueue.guildId eq guildId.asLong() }
    }
}
