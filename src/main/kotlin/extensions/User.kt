package xyz.swagbot.extensions

import discord4j.core.`object`.entity.User
import io.facet.core.feature
import xyz.swagbot.features.permissions.PermissionType
import xyz.swagbot.features.permissions.Permissions

private val User.permissionFeature: Permissions
    get() = client.feature(Permissions)

private suspend fun User.botPermission(): PermissionType {
    return if (permissionFeature.isDeveloper(id))
        PermissionType.DEV
    else
        PermissionType.NORMAL
}

suspend fun User.hasBotPermission(permission: PermissionType): Boolean = botPermission() >= permission
