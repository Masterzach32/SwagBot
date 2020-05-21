package xyz.swagbot.features.guilds

import discord4j.core.event.domain.guild.*
import io.facet.discord.event.*
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.*
import xyz.swagbot.*
import xyz.swagbot.features.system.*

class GuildInitialization(
    val feature: GuildStorage
) : Listener<GuildCreateEvent> {

    override suspend fun on(event: GuildCreateEvent) {
        if (feature.hasGuild(event.guild.id)) {
            logger.info("New guild joined with id ${event.guild.id}, adding to database.")
            sql {
                GuildTable.insert {
                    it[guildId] = event.guild.id.asLong()
                }
            }.await()
        }

        coroutineScope {
            feature.tasks.map { async { it.invoke(event) } }.forEach { it.await() }
        }
    }
}