package xyz.swagbot.features.guilds

import discord4j.common.util.Snowflake
import io.facet.exposed.snowflake
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.Table

object GuildTable : Table("guilds") {
    val guildId = snowflake("guild_id")
    val commandPrefix = varchar("prefix", 10).default("~")

    override val primaryKey = PrimaryKey(guildId)

    fun whereGuildIs(guildId: Snowflake): SqlExpressionBuilder.() -> Op<Boolean> {
        return { GuildTable.guildId eq guildId }
    }
}