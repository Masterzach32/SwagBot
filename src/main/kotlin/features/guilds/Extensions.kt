package xyz.swagbot.features.guilds

import discord4j.core.*
import discord4j.core.`object`.entity.*
import discord4j.core.`object`.util.*
import io.facet.core.extensions.*
import io.facet.discord.extensions.*
import java.util.*

fun DiscordClient.commandPrefixFor(guildId: Snowflake) = commandPrefixFor(guildId.toOptional())

fun DiscordClient.commandPrefixFor(guildId: Optional<Snowflake>) = feature(GuildStorage)
    .commandPrefixFor(guildId)
