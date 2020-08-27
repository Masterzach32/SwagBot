package xyz.swagbot.features.autoroles

import discord4j.common.util.*
import discord4j.core.*
import discord4j.core.event.domain.guild.*
import io.facet.core.*
import io.facet.discord.*
import io.facet.discord.commands.*
import io.facet.discord.exposed.*
import io.facet.discord.extensions.*
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.*
import xyz.swagbot.*
import xyz.swagbot.features.guilds.*
import xyz.swagbot.features.system.*

class AutoAssignRole private constructor() {

    suspend fun autoAssignedRolesFor(guildId: Snowflake): List<Snowflake> = sql {
        RolesTable.select(RolesTable.whereGuildIs(guildId))
            .map { it[RolesTable.roleId] }
    }

    suspend fun addAutoAssignedRoleFor(guildId: Snowflake, roleId: Snowflake) {
        sql {
            RolesTable.insert {
                it[RolesTable.guildId] = guildId
                it[RolesTable.roleId] = roleId
            }
        }
    }

    suspend fun removeAutoAssignedRole(guildId: Snowflake, roleId: Snowflake) {
        sql {
            RolesTable.deleteWhere { RolesTable.guildId.eq(guildId) and RolesTable.roleId.eq(roleId) }
        }
    }

    companion object : DiscordClientFeature<EmptyConfig, AutoAssignRole>(
        keyName = "autoAssignRole",
        requiredFeatures = listOf(GuildStorage, ChatCommands)
    ) {

        override fun install(client: GatewayDiscordClient, configuration: EmptyConfig.() -> Unit): AutoAssignRole {
            runBlocking { sql { create(RolesTable) } }

            return AutoAssignRole().also { feature ->
                client.listener<MemberJoinEvent> {
                    if (member.isBot)
                        return@listener // ignore bots

                    val roleIds = feature.autoAssignedRolesFor(guildId)
                    logger.info("Adding roles: $roleIds to user: ${member.id}")

                    roleIds
                        .map { member.addRole(it, "Auto assigned role") }
                        .forEach { it.await() }
                }
            }
        }
    }
}
