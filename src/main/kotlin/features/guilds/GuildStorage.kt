package xyz.swagbot.features.guilds

import discord4j.core.*
import discord4j.core.`object`.util.*
import discord4j.core.event.domain.guild.*
import io.facet.core.extensions.*
import io.facet.discord.*
import io.facet.discord.extensions.*
import org.jetbrains.exposed.sql.*
import xyz.swagbot.*
import xyz.swagbot.database.*
import java.util.*

class GuildStorage(config: Config) {

    private val tasks = mutableListOf<(GuildCreateEvent) -> Unit>()

    fun hasGuild(id: Snowflake) = sql { GuildTable.select { GuildTable.guildId eq id.asLong() }.firstOrNull() != null }

    fun commandPrefixFor(guildId: Optional<Snowflake>): String = sql {
        guildId.flatMap { id ->
            GuildTable.select(GuildTable.whereGuildIs(id))
                .firstOrNull()
                .toOptional()
                .map { it[GuildTable.commandPrefix] }
        }.orElse("~")
    }

    fun updateCommandPrefixFor(guildId: Snowflake, commandPrefix: String) = sql {
        GuildTable.update(GuildTable.whereGuildIs(guildId)) {
            it[GuildTable.commandPrefix] = commandPrefix
        }
    }

    fun addTaskOnInitialization(task: (GuildCreateEvent) -> Unit) = tasks.add(task)

    class Config

    companion object : DiscordClientFeature<Config, GuildStorage>("guildStorage") {

        override fun install(client: DiscordClient, configuration: Config.() -> Unit): GuildStorage {
            sql { create(GuildTable) }

            return GuildStorage(Config().apply(configuration)).also { feature ->

                client.listen<GuildCreateEvent>()
                    .map { event ->
                        if (!feature.hasGuild(event.guild.id)) {
                            logger.info("New guild joined with id ${event.guild.id}, adding to database.")
                            sql { GuildTable.insert { it[guildId] = event.guild.id.asLong() } }
                        }
                        logger.debug("Running initialization tasks for guild with id ${event.guild.id}.")
                        feature.tasks.forEach { it.invoke(event) }
                    }
                    .subscribe()
            }
        }
    }
}
