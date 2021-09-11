package xyz.swagbot.features.permissions

import discord4j.common.util.Snowflake
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.EventDispatcher
import io.facet.common.await
import io.facet.core.EventDispatcherFeature
import io.facet.core.features.ChatCommands
import io.facet.exposed.create
import io.facet.exposed.sql
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import xyz.swagbot.features.guilds.GuildStorage

class Permissions(config: Config) {

    private val developers: Set<Snowflake> = config.developers.map { Snowflake.of(it) }.toSet()

    suspend fun permissionLevelFor(
        gateway: GatewayDiscordClient,
        guildId: Snowflake,
        userId: Snowflake
    ): PermissionType {
        return when {
            isDeveloper(userId) -> PermissionType.DEV
            isGuildOwner(gateway, guildId, userId) -> PermissionType.ADMIN
            else -> {
                sql {
                    PermissionsTable.select(PermissionsTable.where(guildId, userId))
                        .firstOrNull()
                        ?.let { it[PermissionsTable.permission] }
                        ?: PermissionType.NORMAL
                }
            }
        }
    }

    suspend fun updatePermissionFor(
        gateway: GatewayDiscordClient,
        guildId: Snowflake,
        userId: Snowflake,
        newLevel: PermissionType,
        assignedById: Snowflake
    ): Boolean {
        if (!isDeveloper(userId) && !isGuildOwner(gateway, guildId, userId)) {
            sql {
                PermissionsTable.deleteWhere(op = PermissionsTable.where(guildId, userId))
                PermissionsTable.insert {
                    it[PermissionsTable.guildId] = guildId
                    it[PermissionsTable.userId] = userId
                    it[permission] = newLevel
                    it[PermissionsTable.assignedById] = assignedById
                }
            }
            return true
        }
        return false
    }

    internal fun isDeveloper(userId: Snowflake) = developers.contains(userId)

    private suspend fun isGuildOwner(gateway: GatewayDiscordClient, guildId: Snowflake, userId: Snowflake): Boolean {
        return gateway.getGuildById(guildId).await().ownerId == userId
    }

    class Config {
        lateinit var developers: Set<Long>
    }

    companion object : EventDispatcherFeature<Config, Permissions>(
        "permissions",
        requiredFeatures = listOf(GuildStorage, ChatCommands)
    ) {

        override suspend fun EventDispatcher.install(
            scope: CoroutineScope,
            configuration: Config.() -> Unit
        ): Permissions {
            sql { create(PermissionsTable) }

            return Permissions(Config().apply(configuration))
        }
    }
}
