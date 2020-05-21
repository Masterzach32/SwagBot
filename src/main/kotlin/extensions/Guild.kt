package xyz.swagbot.extensions

import discord4j.core.`object`.entity.*
import discord4j.core.`object`.util.*
import io.facet.core.extensions.*
import io.facet.discord.extensions.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactive.*
import kotlinx.coroutines.reactor.*
import reactor.core.publisher.*
import xyz.swagbot.features.autoroles.*
import xyz.swagbot.features.guilds.*
import xyz.swagbot.features.music.*

suspend fun Guild.getVoiceChannelByName(name: String): VoiceChannel = channels
    .filter { it.name == name }
    .awaitFirst() as VoiceChannel

private val Guild.storageFeature: GuildStorage
    get() = client.feature(GuildStorage)

private val Guild.musicFeature: Music
    get() = client.feature(Music)

private val Guild.aarFeature: AutoAssignRole
    get() = client.feature(AutoAssignRole)

suspend fun Guild.getCommandPrefix(): String = storageFeature.commandPrefixFor(id.toOptional())

suspend fun Guild.updateCommandPrefix(prefix: String) = storageFeature.updateCommandPrefixFor(id, prefix)

suspend fun Guild.isPremium(): Boolean = musicFeature.isEnabledFor(id)

suspend fun Guild.setIsPremium(premium: Boolean) = musicFeature.updateIsEnabledFor(id, premium)

suspend fun Guild.getVolume(): Int = musicFeature.volumeFor(id)

suspend fun Guild.setVolume(volume: Int) = musicFeature.updateVolumeFor(id, volume)

suspend fun Guild.getCurrentlyConnectedChannelId(): Snowflake? = musicFeature.currentlyConnectedChannelFor(id)

suspend fun Guild.getCurrentlyConnectedChannel(): VoiceChannel? = getCurrentlyConnectedChannelId()
    ?.let { client.getChannelById(it).await() as VoiceChannel }

suspend fun Guild.getShouldLoop(): Boolean = musicFeature.shouldLoopFor(id)

suspend fun Guild.toggleShouldLoop() = musicFeature.toggleShouldLoopFor(id)

suspend fun Guild.getAutoplay(): Boolean = musicFeature.shouldAutoplayFor(id)

suspend fun Guild.toggleAutoplay() = musicFeature.toggleShouldAutoplayFor(id)

suspend fun Guild.getAutoAssignedRoleIds(): List<Snowflake> = aarFeature.autoAssignedRolesFor(id)

suspend fun Guild.getAutoAssignedRoles(): Flow<Role> = getAutoAssignedRoleIds().asFlow()
    .map { getRoleById(it).await() }
