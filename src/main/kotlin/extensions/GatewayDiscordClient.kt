package xyz.swagbot.extensions

import discord4j.common.util.*
import discord4j.core.*
import io.facet.core.*
import xyz.swagbot.features.guilds.*

suspend fun GatewayDiscordClient.commandPrefixFor(guildId: Snowflake?) = feature(GuildStorage).commandPrefixFor(guildId)
