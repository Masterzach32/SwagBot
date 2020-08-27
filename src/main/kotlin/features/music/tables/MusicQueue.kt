package xyz.swagbot.features.music.tables

import discord4j.common.util.*
import io.facet.discord.exposed.columns.*
import org.jetbrains.exposed.sql.*
import xyz.swagbot.features.guilds.*

object MusicQueue : Table("music_queue") {

    val guildId = snowflake("guild_id") references GuildTable.guildId
    val identifier = text("identifier")
    val requesterId = snowflake("requester_id")
    val requestedChannelId = snowflake("requested_channel_id")

    fun whereGuildIs(guildId: Snowflake): SqlExpressionBuilder.()->Op<Boolean> {
        return { this@MusicQueue.guildId eq guildId }
    }
}
