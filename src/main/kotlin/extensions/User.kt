package xyz.swagbot.extensions

import discord4j.core.`object`.entity.*
import io.facet.core.*
import xyz.swagbot.features.permissions.*

private val User.permissionFeature: Permissions
    get() = client.feature(Permissions)

private suspend fun User.botPermission(): PermissionType {
    return if (permissionFeature.isDeveloper(id))
        PermissionType.DEV
    else
        PermissionType.NORMAL
}

suspend fun User.hasBotPermission(permission: PermissionType): Boolean = botPermission() >= permission
