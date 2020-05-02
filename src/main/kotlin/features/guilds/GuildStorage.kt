package xyz.swagbot.features.guilds

import discord4j.core.*
import discord4j.core.`object`.util.*
import discord4j.core.event.domain.guild.*
import io.facet.core.extensions.*
import io.facet.discord.*
import io.facet.discord.extensions.*
import org.jetbrains.exposed.sql.*
import reactor.core.publisher.*
import xyz.swagbot.*
import xyz.swagbot.database.*
import xyz.swagbot.features.*
import java.util.*

class GuildStorage private constructor() {

    private val tasks = mutableListOf<(GuildCreateEvent) -> Mono<Void>>()

    fun hasGuild(id: Snowflake): Mono<Boolean> = sql {
        GuildTable.select { GuildTable.guildId eq id.asLong() }.firstOrNull() != null
    }

    fun commandPrefixFor(guildId: Optional<Snowflake>): Mono<String> = sql {
        guildId.flatMap { id ->
            GuildTable.select(GuildTable.whereGuildIs(id))
                .firstOrNull()
                .toOptional()
                .map { it[GuildTable.commandPrefix] }
        }.orElse("~")
    }

    fun updateCommandPrefixFor(guildId: Snowflake, commandPrefix: String): Mono<Void> = sql {
        GuildTable.update(GuildTable.whereGuildIs(guildId)) {
            it[GuildTable.commandPrefix] = commandPrefix
        }
    }.then()

    fun addTaskOnGuildInitialization(task: (GuildCreateEvent) -> Mono<Void>) = tasks.add(task)

    companion object : DiscordClientFeature<EmptyConfig, GuildStorage>("guildStorage") {

        override fun install(client: DiscordClient, configuration: EmptyConfig.() -> Unit): GuildStorage {
            sql { create(GuildTable) }

            return GuildStorage().also { feature ->
                client.listen<GuildCreateEvent>()
                    .flatMap { event ->
                        feature.hasGuild(event.guild.id)
                            .filter { !it }
                            .flatMap {
                                logger.info("New guild joined with id ${event.guild.id}, adding to database.")
                                sql { GuildTable.insert { it[guildId] = event.guild.id.asLong() } }
                            }
                            .then(feature.tasks.toFlux().flatMap { it.invoke(event) }.then())
                    }
                    .subscribe()
            }
        }
    }
}
