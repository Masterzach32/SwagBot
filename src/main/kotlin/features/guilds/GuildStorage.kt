package xyz.swagbot.features.guilds

import discord4j.common.util.*
import discord4j.core.*
import discord4j.core.event.domain.guild.*
import io.facet.core.*
import io.facet.core.extensions.*
import io.facet.discord.*
import io.facet.discord.exposed.*
import io.facet.discord.extensions.*
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.*
import xyz.swagbot.*
import xyz.swagbot.features.system.*
import java.util.*

class GuildStorage private constructor() {

    val tasks = mutableListOf<GuildInitializationTask>()

    suspend fun hasGuild(id: Snowflake): Boolean = sql {
        GuildTable.select { GuildTable.guildId eq id }.any()
    }

    suspend fun commandPrefixFor(guildId: Optional<Snowflake>): String = sql {
        guildId.flatMap { id ->
            GuildTable.select(GuildTable.whereGuildIs(id))
                .firstOrNull()
                .toOptional()
                .map { it[GuildTable.commandPrefix] }
        }.orElse("~")
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

            return GuildStorage().also { feature ->
                client.listener<GuildCreateEvent> {
                    if (!feature.hasGuild(guild.id)) {
                        logger.info("New guild joined with id ${guild.id}, adding to database.")
                        sql {
                            GuildTable.insert {
                                it[guildId] = guild.id
                            }
                        }
                    }

                    coroutineScope {
                        feature.tasks.forEach {
                            launch { it.invoke(this@listener) }
                        }
                    }
                }
            }
        }
    }
}
