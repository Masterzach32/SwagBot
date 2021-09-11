package xyz.swagbot.extensions

import io.facet.chatcommands.ChatCommandSource
import xyz.swagbot.features.permissions.PermissionType

suspend fun ChatCommandSource.hasBotPermission(
    permission: PermissionType
): Boolean = if (this.guildId != null) member.hasBotPermission(permission) else user.hasBotPermission(permission)

suspend fun ChatCommandSource.isMusicFeatureEnabled(): Boolean = getGuild().isPremium()
