package xyz.swagbot.features.permissions

import discord4j.core.*
import discord4j.core.`object`.util.*
import io.facet.discord.*
import io.facet.discord.commands.*
import io.facet.discord.extensions.*
import org.jetbrains.exposed.sql.*
import xyz.swagbot.database.*
import xyz.swagbot.features.guilds.*

class Permissions(config: Config, private val client: DiscordClient) {

    private val developers = config.developers.map { Snowflake.of(it) }

    val commands = listOf(ChangePermissionCommand)

    fun permissionLevelFor(guildId: Snowflake, userId: Snowflake): PermissionType {
        return when {
            isDeveloper(userId) -> PermissionType.DEV
            isGuildOwner(guildId, userId) -> PermissionType.ADMIN
            else -> {
                sql {
                    PermissionsTable.select(PermissionsTable.where(guildId, userId))
                        .firstOrNull()
                        ?.let { it[PermissionsTable.permission] }
                } ?: PermissionType.NORMAL
            }
        }
    }

    fun updatePermissionFor(
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
                    it[PermissionsTable.permission] = newLevel
                    it[PermissionsTable.assignedById] = assignedById.asLong()
                }
            }
            return true
        }
        return false
    }

    internal fun isDeveloper(userId: Snowflake) = developers.contains(userId)

    private fun isGuildOwner(guildId: Snowflake, userId: Snowflake): Boolean = client
        .getGuildById(guildId)
        .map { it.ownerId == userId }
        .block()!!

    class Config {
        lateinit var developers: List<Long>
    }

    companion object : DiscordClientFeature<Config, Permissions>("permissions", listOf(GuildStorage, ChatCommands)) {

        override fun install(client: DiscordClient, configuration: Config.() -> Unit): Permissions {
            sql { create(PermissionsTable) }

            return Permissions(Config().apply(configuration), client).also { feature ->
                client.feature(ChatCommands).dispatcher.apply { feature.commands.forEach { it.register(this) } }
            }
        }
    }
}
