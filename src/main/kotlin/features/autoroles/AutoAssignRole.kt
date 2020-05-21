package xyz.swagbot.features.autoroles

import discord4j.core.*
import discord4j.core.`object`.util.*
import discord4j.core.event.domain.guild.*
import io.facet.core.*
import io.facet.discord.*
import io.facet.discord.commands.*
import io.facet.discord.extensions.*
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.*
import reactor.core.publisher.*
import xyz.swagbot.*
import xyz.swagbot.features.guilds.*
import xyz.swagbot.features.system.*

class AutoAssignRole private constructor() {

    suspend fun autoAssignedRolesFor(guildId: Snowflake): List<Snowflake> = sql {
        RolesTable.select(RolesTable.whereGuildIs(guildId))
            .map { it[RolesTable.roleId].toSnowflake() }
    }

    suspend fun addAutoAssignedRoleFor(guildId: Snowflake, roleId: Snowflake) {
        sql {
            RolesTable.insert {
                it[RolesTable.guildId] = guildId.asLong()
                it[RolesTable.roleId] = roleId.asLong()
            }
        }
    }

    suspend fun removeAutoAssignedRole(guildId: Snowflake, roleId: Snowflake) {
        sql {
            RolesTable.deleteWhere { RolesTable.guildId.eq(guildId.asLong()) and RolesTable.roleId.eq(roleId.asLong()) }
        }
    }

    companion object : DiscordClientFeature<EmptyConfig, AutoAssignRole>(
        keyName = "autoAssignRole",
        requiredFeatures = listOf(GuildStorage, ChatCommands)
    ) {

        override fun install(client: DiscordClient, configuration: EmptyConfig.() -> Unit): AutoAssignRole {
            runBlocking { sql { create(RolesTable) } }

            return AutoAssignRole()
        }
    }
}
