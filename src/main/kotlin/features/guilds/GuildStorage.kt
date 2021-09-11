package xyz.swagbot.features.guilds

import discord4j.common.util.Snowflake
import discord4j.core.event.EventDispatcher
import discord4j.core.event.domain.guild.GuildCreateEvent
import io.facet.common.listener
import io.facet.core.EmptyConfig
import io.facet.core.EventDispatcherFeature
import io.facet.exposed.create
import io.facet.exposed.sql
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import xyz.swagbot.EnvVars
import xyz.swagbot.logger

class GuildStorage private constructor() {

    val tasks = mutableListOf<suspend (GuildCreateEvent) -> Unit>()

    suspend fun hasGuild(id: Snowflake): Boolean = sql {
        GuildTable.select { GuildTable.guildId eq id }.any()
    }

    suspend fun commandPrefixFor(guildId: Snowflake?): String = when (guildId) {
        null -> EnvVars.DEFAULT_COMMAND_PREFIX
        else -> sql {
            GuildTable.select(GuildTable.whereGuildIs(guildId)).first()[GuildTable.commandPrefix]
        }
    }

    suspend fun updateCommandPrefixFor(guildId: Snowflake, commandPrefix: String) {
        sql {
            GuildTable.update(GuildTable.whereGuildIs(guildId)) {
                it[GuildTable.commandPrefix] = commandPrefix
            }
        }
    }

    fun addTaskOnGuildInitialization(task: suspend (GuildCreateEvent) -> Unit) = tasks.add(task)

    companion object : EventDispatcherFeature<EmptyConfig, GuildStorage>("guildStorage") {

        override suspend fun EventDispatcher.install(
            scope: CoroutineScope,
            configuration: EmptyConfig.() -> Unit
        ): GuildStorage {
            sql { create(GuildTable) }

            return GuildStorage().apply {
                listener<GuildCreateEvent>(scope) { event ->
                    if (!hasGuild(event.guild.id)) {
                        logger.info("New guild joined with id ${event.guild.id}, adding to database.")
                        sql {
                            GuildTable.insert {
                                it[guildId] = event.guild.id
                            }
                        }
                    }

                    coroutineScope {
                        tasks.forEach {
                            launch { it.invoke(event) }
                        }
                    }
                }
            }
        }
    }
}
