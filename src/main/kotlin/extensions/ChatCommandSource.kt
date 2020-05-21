package xyz.swagbot.extensions

import io.facet.core.extensions.*
import io.facet.discord.commands.*
import io.facet.discord.extensions.*
import xyz.swagbot.features.permissions.*

suspend fun ChatCommandSource.hasBotPermission(permission: PermissionType): Boolean = client.feature(Permissions)
    .let { feature ->
        member.grab()?.let { member ->
            feature.permissionLevelFor(event.guildId.get(), member.id) >= permission
        } ?: user.map { feature.isDeveloper(it.id) }.orElse(false)
    }

suspend fun ChatCommandSource.isMusicFeatureEnabled(): Boolean = guild.awaitNull()?.isPremium() ?: false

//fun ChatCommandSource.handleBotPerm(
//    permission: PermissionType,
//    errorMessage: String
//): Mono<Void> = hasBotPermission(permission).toMono()
//    .filter { !it }
//    .flatMap { _ ->
//        channel.flatMap { channel ->
//            channel.createEmbed(errorTemplate.andThen {
//                it.setDescription(errorMessage)
//            })
//        }
//    }
//    .then()
//
//fun ChatCommandSource.handlePremium(): Mono<Void> = isMusicFeatureEnabled().toMono()
//    .filter { !it }
//    .flatMap { _ ->
//        channel.flatMap { channel ->
//            channel.createEmbed(errorTemplate.andThen {
//                it.setDescription("Music commands are a premium feature of SwagBot. Type " +
//                        "`${commandPrefixUsed}premium` to learn more.")
//            })
//        }
//    }
//    .then()
