package xyz.swagbot.extensions

import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Role
import discord4j.core.`object`.entity.channel.VoiceChannel
import io.facet.common.await
import io.facet.core.feature
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import xyz.swagbot.features.autoroles.AutoAssignRole
import xyz.swagbot.features.guilds.GuildStorage
import xyz.swagbot.features.music.Music
import xyz.swagbot.features.music.TrackScheduler

private val Guild.storageFeature: GuildStorage
    get() = client.feature(GuildStorage)

private val Guild.musicFeature: Music
    get() = client.feature(Music)

private val Guild.aarFeature: AutoAssignRole
    get() = client.feature(AutoAssignRole)

suspend fun Guild.getCommandPrefix(): String = storageFeature.commandPrefixFor(id)

suspend fun Guild.updateCommandPrefix(prefix: String) = storageFeature.updateCommandPrefixFor(id, prefix)

suspend fun Guild.isPremium(): Boolean = musicFeature.isEnabledFor(id)

suspend fun Guild.setIsPremium(premium: Boolean) = musicFeature.updateIsEnabledFor(id, premium)

val Guild.trackScheduler: TrackScheduler
    get() = musicFeature.trackSchedulerFor(id)

fun Guild.getVolume(): Int = musicFeature.getVolumeFor(id)

suspend fun Guild.setVolume(volume: Int) = musicFeature.updateVolumeFor(id, volume)

suspend fun Guild.getLastConnectedChannelId(): Snowflake? = musicFeature.lastConnectedChannelFor(id)

suspend fun Guild.getLastConnectedChannel(): VoiceChannel? = getLastConnectedChannelId()
    ?.let { client.getChannelById(it).await() as VoiceChannel }

suspend fun Guild.setLastConnectedChannel(
    channelId: Snowflake?
) = musicFeature.updateLastConnectedChannelFor(id, channelId)

fun Guild.getShouldLoop(): Boolean = musicFeature.shouldLoopFor(id)

suspend fun Guild.setShouldLoop(loop: Boolean) = musicFeature.toggleShouldLoopFor(id, loop)

fun Guild.getAutoplay(): Boolean = musicFeature.shouldAutoplayFor(id)

suspend fun Guild.toggleAutoplay(autoplay: Boolean) = musicFeature.setShouldAutoplayFor(id, autoplay)

suspend fun Guild.getAutoAssignedRoleIds(): List<Snowflake> = aarFeature.autoAssignedRolesFor(id)

suspend fun Guild.getAutoAssignedRoles(): Flow<Role> = getAutoAssignedRoleIds().asFlow()
    .map { getRoleById(it).await() }
