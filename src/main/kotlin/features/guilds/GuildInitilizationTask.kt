package xyz.swagbot.features.guilds

import discord4j.core.event.domain.guild.*

typealias GuildInitializationTask = suspend (GuildCreateEvent) -> Unit