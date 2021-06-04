package xyz.swagbot.features.permissions

import discord4j.common.util.*
import discord4j.core.*
import io.facet.discord.*
import io.facet.discord.commands.*
import io.facet.discord.exposed.*
import io.facet.discord.extensions.*
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.*
import xyz.swagbot.features.guilds.*

class Permissions(config: Config, private val client: GatewayDiscordClient) {

    private val developers: Set<Snowflake> = config.developers.map { Snowflake.of(it) }.toSet()

    suspend fun permissionLevelFor(guildId: Snowflake, userId: Snowflake): PermissionType {
        return when {
            isDeveloper(userId) -> PermissionType.DEV
            isGuildOwner(guildId, userId) -> PermissionType.ADMIN
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
        guildId: Snowflake,
        userId: Snowflake,
        newLevel: PermissionType,
        assignedById: Snowflake
    ): Boolean {
        if (!isDeveloper(userId) && !isGuildOwner(guildId, userId)) {
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

    private suspend fun isGuildOwner(guildId: Snowflake, userId: Snowflake): Boolean {
        return client.getGuildById(guildId).await().ownerId == userId
    }

    class Config {
        lateinit var developers: Set<Long>
    }

    companion object : GatewayFeature<Config, Permissions>(
        "permissions",
        requiredFeatures = listOf(GuildStorage, ChatCommands)
    ) {

        override fun GatewayDiscordClient.install(scope: CoroutineScope, configuration: Config.() -> Unit): Permissions {
            runBlocking { sql { create(PermissionsTable) } }

            return Permissions(Config().apply(configuration), this)
        }
    }
}
