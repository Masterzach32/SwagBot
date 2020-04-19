package xyz.swagbot.features.permissions

import discord4j.core.`object`.util.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.jodatime.*
import org.joda.time.*
import xyz.swagbot.features.guilds.*

object PermissionsTable : Table("permissions") {
    val userId = long("user_id")
    val guildId = long("guild_id") references GuildTable.guildId
    val permission = enumeration("permission", PermissionType::class)
    val assignedOn = datetime("assigned_on").clientDefault { DateTime.now() }
    val assignedById = long("assigned_by_id")

    override val primaryKey = PrimaryKey(userId, guildId)

    fun where(guildId: Snowflake, userId: Snowflake): SqlExpressionBuilder.()->Op<Boolean> {
        return { this@PermissionsTable.guildId.eq(guildId.asLong()) and this@PermissionsTable.userId.eq(userId.asLong()) }
    }
}
