package xyz.swagbot.extensions

import discord4j.core.`object`.entity.*
import io.facet.discord.extensions.*
import xyz.swagbot.features.permissions.*

private val Member.permissionFeature: Permissions
    get() = client.feature(Permissions)

suspend fun Member.botPermission(): PermissionType = permissionFeature.permissionLevelFor(client, guildId, id)

suspend fun Member.hasBotPermission(permission: PermissionType): Boolean = botPermission() >= permission

suspend fun Member.updateBotPermission(
    permission: PermissionType,
    assignedBy: Member
) = permissionFeature.updatePermissionFor(client, guildId, id, permission, assignedBy.id)
