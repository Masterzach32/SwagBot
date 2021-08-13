package xyz.swagbot.features.autoroles

import discord4j.common.util.*
import io.facet.exposed.*
import org.jetbrains.exposed.sql.*
import xyz.swagbot.features.guilds.*

object RolesTable : Table("auto_assigned_roles") {
    val guildId = snowflake("guild_id") references GuildTable.guildId
    val roleId = snowflake("role_id")

    override val primaryKey = PrimaryKey(guildId, roleId)

    fun whereGuildIs(guildId: Snowflake): SqlExpressionBuilder.() -> Op<Boolean> {
        return { this@RolesTable.guildId eq guildId }
    }
}
