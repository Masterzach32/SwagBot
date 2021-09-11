package xyz.swagbot.extensions

import discord4j.common.util.Snowflake
import discord4j.core.GatewayDiscordClient
import io.facet.core.feature
import xyz.swagbot.features.guilds.GuildStorage

suspend fun GatewayDiscordClient.commandPrefixFor(guildId: Snowflake?) = feature(GuildStorage).commandPrefixFor(guildId)
