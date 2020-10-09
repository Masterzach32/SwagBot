package xyz.swagbot.features.guilds

import discord4j.common.util.*
import discord4j.core.*
import discord4j.core.event.domain.guild.*
import io.facet.core.*
import io.facet.discord.*
import io.facet.discord.event.*
import io.facet.discord.exposed.*
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.*
import xyz.swagbot.*
import xyz.swagbot.features.system.*

class GuildStorage private constructor() {

    val tasks = mutableListOf<GuildInitializationTask>()

    suspend fun hasGuild(id: Snowflake): Boolean = sql {
        GuildTable.select { GuildTable.guildId eq id }.any()
    }

    suspend fun commandPrefixFor(guildId: Snowflake?): String = when(guildId) {
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

    fun addTaskOnGuildInitialization(task: GuildInitializationTask) = tasks.add(task)

    companion object : DiscordClientFeature<EmptyConfig, GuildStorage>("guildStorage") {

        override fun install(client: GatewayDiscordClient, configuration: EmptyConfig.() -> Unit): GuildStorage {
            runBlocking {
                sql { create(GuildTable) }
            }

            return GuildStorage().apply {
                BotScope.listener<GuildCreateEvent>(client) { event ->
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
