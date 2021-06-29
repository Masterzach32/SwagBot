package xyz.swagbot.features.guilds

import discord4j.common.util.*
import io.facet.discord.exposed.*
import org.jetbrains.exposed.sql.*

object GuildTable : Table("guilds") {
    val guildId = snowflake("guild_id")
    val commandPrefix = varchar("prefix", 10).default("~")

    override val primaryKey = PrimaryKey(guildId)

    fun whereGuildIs(guildId: Snowflake): SqlExpressionBuilder.()->Op<Boolean> {
        return { GuildTable.guildId eq guildId }
    }
}