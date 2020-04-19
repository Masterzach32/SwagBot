package xyz.swagbot.features.guilds

import discord4j.core.`object`.util.*
import org.jetbrains.exposed.sql.*

object GuildTable : Table("guilds") {
    val guildId = long("guild_id")
    val commandPrefix = varchar("prefix", 6).default("~")

    override val primaryKey = PrimaryKey(guildId)

    fun whereGuildIs(guildId: Snowflake): SqlExpressionBuilder.()->Op<Boolean> {
        return { GuildTable.guildId eq guildId.asLong() }
    }
}