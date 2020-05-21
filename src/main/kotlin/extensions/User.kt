package xyz.swagbot.extensions

import discord4j.core.`object`.entity.*
import io.facet.discord.extensions.*
import xyz.swagbot.features.permissions.*

private val User.permissionFeature: Permissions
    get() = client.feature(Permissions)

suspend fun User.botPermission(): PermissionType {
    return if (permissionFeature.isDeveloper(id))
        PermissionType.DEV
    else
        PermissionType.NORMAL
}

suspend fun User.hasBotPermission(permission: PermissionType): Boolean = botPermission() >= permission
