package xyz.swagbot.features.autoroles

import discord4j.common.util.Snowflake
import io.facet.exposed.snowflake
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.Table
import xyz.swagbot.features.guilds.GuildTable

object RolesTable : Table("auto_assigned_roles") {
    val guildId = snowflake("guild_id") references GuildTable.guildId
    val roleId = snowflake("role_id")

    override val primaryKey = PrimaryKey(guildId, roleId)

    fun whereGuildIs(guildId: Snowflake): SqlExpressionBuilder.() -> Op<Boolean> {
        return { this@RolesTable.guildId eq guildId }
    }
}
