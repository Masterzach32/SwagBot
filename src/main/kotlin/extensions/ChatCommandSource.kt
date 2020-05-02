package xyz.swagbot.extensions

import io.facet.discord.commands.*
import io.facet.discord.extensions.*
import xyz.swagbot.features.permissions.*

fun ChatCommandSource.hasBotPermission(permission: PermissionType): Boolean = client.feature(Permissions)
    .let { feature ->
        member.map { member ->
            feature.permissionLevelFor(event.guildId.get(), member.id) >= permission
        }.orElseGet {
            user.map { feature.isDeveloper(it.id) }.orElse(false)
        }
    }
