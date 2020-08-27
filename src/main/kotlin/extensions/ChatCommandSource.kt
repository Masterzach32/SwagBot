package xyz.swagbot.extensions

import io.facet.discord.commands.*
import xyz.swagbot.features.permissions.*

suspend fun ChatCommandSource.hasBotPermission(
    permission: PermissionType
): Boolean = member?.hasBotPermission(permission) ?: user.hasBotPermission(PermissionType.DEV)

suspend fun ChatCommandSource.isMusicFeatureEnabled(): Boolean = getGuild().isPremium()
