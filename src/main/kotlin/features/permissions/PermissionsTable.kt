package xyz.swagbot.features.permissions

import discord4j.common.util.Snowflake
import io.facet.exposed.snowflake
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.and
import xyz.swagbot.features.guilds.GuildTable
import java.time.LocalDateTime

object PermissionsTable : Table("permissions") {
    val userId = snowflake("user_id")
    val guildId = snowflake("guild_id") references GuildTable.guildId
    val permission = enumeration("permission", PermissionType::class)
    val assignedOn = datetime("assigned_on").clientDefault { LocalDateTime.now() }
    val assignedById = snowflake("assigned_by_id")

    override val primaryKey = PrimaryKey(userId, guildId)

    fun where(guildId: Snowflake, userId: Snowflake): SqlExpressionBuilder.() -> Op<Boolean> {
        return { this@PermissionsTable.guildId.eq(guildId) and this@PermissionsTable.userId.eq(userId) }
    }
}
