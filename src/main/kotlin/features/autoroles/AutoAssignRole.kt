package xyz.swagbot.features.autoroles

import discord4j.common.util.Snowflake
import discord4j.core.event.EventDispatcher
import discord4j.core.event.domain.guild.MemberJoinEvent
import io.facet.common.await
import io.facet.common.awaitNullable
import io.facet.common.listener
import io.facet.core.EmptyConfig
import io.facet.core.EventDispatcherFeature
import io.facet.core.feature
import io.facet.core.features.ChatCommands
import io.facet.exposed.create
import io.facet.exposed.sql
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import xyz.swagbot.features.guilds.GuildStorage
import xyz.swagbot.logger

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

        override suspend fun EventDispatcher.install(
            scope: CoroutineScope,
            configuration: EmptyConfig.() -> Unit
        ): AutoAssignRole {
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
