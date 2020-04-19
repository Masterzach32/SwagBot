package xyz.swagbot.features.permissions

import io.facet.discord.commands.*
import io.facet.discord.extensions.*

fun ChatCommandSource.hasBotPermission(permission: PermissionType): Boolean = member.map { member ->
    client.feature(Permissions).permissionLevelFor(event.guildId.get(), member.id) >= permission
}.orElseGet {
    user.map { user ->
        client.feature(Permissions).isDeveloper(user.id)
    }.orElse(false)
}
