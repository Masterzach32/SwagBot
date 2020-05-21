package xyz.swagbot.features.permissions

import discord4j.core.*
import discord4j.core.`object`.util.*
import io.facet.discord.*
import io.facet.discord.commands.*
import io.facet.discord.extensions.*
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.*
import xyz.swagbot.features.guilds.*
import xyz.swagbot.features.system.*

class Permissions(config: Config, private val client: DiscordClient) {

    private val developers = config.developers.map { Snowflake.of(it) }

    val commands = listOf(ChangePermissionCommand)

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
                    it[PermissionsTable.guildId] = guildId.asLong()
                    it[PermissionsTable.userId] = userId.asLong()
                    it[permission] = newLevel
                    it[PermissionsTable.assignedById] = assignedById.asLong()
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
        lateinit var developers: List<Long>
    }

    companion object : DiscordClientFeature<Config, Permissions>("permissions", listOf(GuildStorage, ChatCommands)) {

        override fun install(client: DiscordClient, configuration: Config.() -> Unit): Permissions {
            runBlocking { sql { create(PermissionsTable) } }

            return Permissions(Config().apply(configuration), client).also { feature ->
                client.feature(ChatCommands).registerCommands(*feature.commands.toTypedArray())
            }
        }
    }
}
