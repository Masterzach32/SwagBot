package xyz.swagbot.extensions

import discord4j.core.*
import discord4j.core.`object`.util.*
import io.facet.core.extensions.*
import io.facet.discord.extensions.*
import xyz.swagbot.features.guilds.*

suspend fun DiscordClient.commandPrefixFor(guildId: Snowflake) = feature(GuildStorage).commandPrefixFor(guildId.toOptional())
