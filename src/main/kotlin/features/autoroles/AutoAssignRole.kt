package xyz.swagbot.features.autoroles

import discord4j.core.*
import discord4j.core.`object`.util.*
import discord4j.core.event.domain.guild.*
import io.facet.discord.*
import io.facet.discord.commands.*
import io.facet.discord.extensions.*
import org.jetbrains.exposed.sql.*
import reactor.core.publisher.*
import xyz.swagbot.*
import xyz.swagbot.database.*
import xyz.swagbot.features.*
import xyz.swagbot.features.guilds.*

class AutoAssignRole private constructor() {

    fun autoAssignedRolesFor(guildId: Snowflake): Mono<List<Snowflake>> = sql {
        RolesTable.select(RolesTable.whereGuildIs(guildId))
            .map { it[RolesTable.roleId].toSnowflake() }
    }

    fun addAutoAssignedRoleFor(guildId: Snowflake, roleId: Snowflake): Mono<Void> = sql {
        RolesTable.insert {
            it[RolesTable.guildId] = guildId.asLong()
            it[RolesTable.roleId] = roleId.asLong()
        }
    }.then()

    fun removeAutoAssignedRole(guildId: Snowflake, roleId: Snowflake): Mono<Void> = sql {
        RolesTable.deleteWhere { RolesTable.guildId.eq(guildId.asLong()) and RolesTable.roleId.eq(roleId.asLong()) }
    }.then()

    companion object : DiscordClientFeature<EmptyConfig, AutoAssignRole>(
        keyName = "autoAssignRole",
        requiredFeatures = listOf(GuildStorage, ChatCommands)
    ) {

        override fun install(client: DiscordClient, configuration: EmptyConfig.() -> Unit): AutoAssignRole {
            sql { create(RolesTable) }

            return AutoAssignRole().also { feature ->
                client.listen<MemberJoinEvent>()
                    .filter { !it.member.isBot }
                    .flatMap { event ->
                        logger.info("New user joined guild: ${event.guildId}")
                        feature.autoAssignedRolesFor(event.guildId)
                            .flatMap { roles ->
                                logger.info("Adding roles: $roles")
                                roles.toFlux()
                                    .flatMap { event.member.addRole(it, "Auto assigned role.") }
                                    .then()
                            }
                    }
                    .subscribe()
            }
        }
    }
}
