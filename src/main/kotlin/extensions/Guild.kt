package xyz.swagbot.extensions

import discord4j.common.util.*
import discord4j.core.`object`.entity.*
import discord4j.core.`object`.entity.channel.*
import discord4j.voice.*
import io.facet.core.extensions.*
import io.facet.discord.extensions.*
import kotlinx.coroutines.flow.*
import xyz.swagbot.features.autoroles.*
import xyz.swagbot.features.guilds.*
import xyz.swagbot.features.music.*

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

val Guild.trackScheduler: TrackScheduler
    get() = musicFeature.trackSchedulerFor(id)

suspend fun Guild.getVoiceConnection(): VoiceConnection? = client.voiceConnectionRegistry.getVoiceConnection(id).await()

suspend fun Guild.getVolume(): Int = musicFeature.volumeFor(id)

suspend fun Guild.setVolume(volume: Int) = musicFeature.updateVolumeFor(id, volume)

suspend fun Guild.getLastConnectedChannelId(): Snowflake? = musicFeature.lastConnectedChannelFor(id)

suspend fun Guild.getLastConnectedChannel(): VoiceChannel? = getLastConnectedChannelId()
    ?.let { client.getChannelById(it).await() as VoiceChannel }

suspend fun Guild.setLastConnectedChannel(
    channelId: Snowflake?
) = musicFeature.updateLastConnectedChannelFor(id, channelId)

suspend fun Guild.getShouldLoop(): Boolean = musicFeature.shouldLoopFor(id)

suspend fun Guild.toggleShouldLoop() = musicFeature.toggleShouldLoopFor(id)

suspend fun Guild.getAutoplay(): Boolean = musicFeature.shouldAutoplayFor(id)

suspend fun Guild.toggleAutoplay() = musicFeature.toggleShouldAutoplayFor(id)

suspend fun Guild.getAutoAssignedRoleIds(): List<Snowflake> = aarFeature.autoAssignedRolesFor(id)

suspend fun Guild.getAutoAssignedRoles(): Flow<Role> = getAutoAssignedRoleIds().asFlow()
    .map { getRoleById(it).await() }
