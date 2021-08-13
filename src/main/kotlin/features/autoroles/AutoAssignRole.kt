package xyz.swagbot.features.autoroles

import discord4j.common.util.*
import discord4j.core.event.*
import discord4j.core.event.domain.guild.*
import io.facet.common.*
import io.facet.core.*
import io.facet.core.features.*
import io.facet.exposed.*
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.*
import xyz.swagbot.*
import xyz.swagbot.features.guilds.*

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

    companion object : EventDispatcherFeature<EmptyConfig, AutoAssignRole>(
        keyName = "autoAssignRole",
        requiredFeatures = listOf(GuildStorage, ChatCommands)
    ) {

        override suspend fun EventDispatcher.install(scope: CoroutineScope, configuration: EmptyConfig.() -> Unit): AutoAssignRole {
            sql { create(RolesTable) }

            return AutoAssignRole().apply {
                listener<MemberJoinEvent>(scope) { event ->
                    if (event.member.isBot)
                        return@listener // ignore bots

                    val roleIds = autoAssignedRolesFor(event.guildId)
                    if (roleIds.isNotEmpty())
                        logger.info("Adding roles: $roleIds to user: ${event.member.id}")

                    roleIds
                        .map { it to event.member.addRole(it, "Auto assigned role") }
                        .forEach { (roleId, request) ->
                            try {
                                request.await()
                            } catch (e: Throwable) {
                                logger.error("Could not add role $roleId to member. Removing from database.", e)
                                sql {
                                    RolesTable.deleteWhere { RolesTable.roleId eq roleId }
                                }
                            }
                        }
                }

                feature(GuildStorage).addTaskOnGuildInitialization { event ->
                    autoAssignedRolesFor(event.guild.id)
                        .map { it to event.guild.getRoleById(it).awaitNullable() }
                        .filter { (_, role) -> role == null }
                        .forEach { (roleId, _) ->
                            logger.error(
                                "Auto assigned role $roleId in database could not be found in guild ${event.guild.id}."
                            )
                            sql {
                                RolesTable.deleteWhere { RolesTable.roleId eq roleId }
                            }
                        }
                }
            }
        }
    }
}
