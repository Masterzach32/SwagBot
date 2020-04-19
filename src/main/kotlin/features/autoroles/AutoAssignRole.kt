package xyz.swagbot.features.autoroles

import discord4j.core.*
import discord4j.core.`object`.util.*
import discord4j.core.event.domain.guild.*
import io.facet.discord.*
import io.facet.discord.extensions.*
import org.jetbrains.exposed.sql.*
import reactor.core.publisher.*
import xyz.swagbot.database.*
import xyz.swagbot.features.guilds.*

class AutoAssignRole(config: Config) {

    fun autoAssignedRolesFor(guildId: Snowflake) = sql {
        RolesTable.select(RolesTable.whereGuildIs(guildId))
            .map { it[RolesTable.roleId].toSnowflake() }
    }

    fun addAutoAssignedRoleFor(guildId: Snowflake, roleId: Snowflake) = sql {
        RolesTable.insert {
            it[RolesTable.guildId] = guildId.asLong()
            it[RolesTable.roleId] = roleId.asLong()
        }
    }

    fun removeAutoAssignedRole(guildId: Snowflake, roleId: Snowflake) = sql {
        RolesTable.deleteWhere { RolesTable.guildId.eq(guildId.asLong()) and RolesTable.roleId.eq(roleId.asLong()) }
    }

    class Config

    companion object : DiscordClientFeature<Config, AutoAssignRole>("autoAssignRole", listOf(GuildStorage)) {

        override fun install(client: DiscordClient, configuration: Config.() -> Unit): AutoAssignRole {
            sql { create(RolesTable) }

            return AutoAssignRole(Config().apply(configuration)).also { feature ->

                client.listen<MemberJoinEvent>()
                    .filter { !it.member.isBot }
                    .flatMap { event ->
                        feature.autoAssignedRolesFor(event.guildId)
                            .toFlux()
                            .flatMap { event.member.addRole(it) }
                    }
                    .subscribe()
            }
        }
    }
}
