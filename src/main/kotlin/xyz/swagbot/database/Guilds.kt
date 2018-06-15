package xyz.swagbot.database

import org.jetbrains.exposed.sql.*
import sx.blah.discord.handle.obj.*
import xyz.swagbot.api.music.SilentAudioTrackLoadHandler
import xyz.swagbot.api.music.TrackHandler
import xyz.swagbot.audioPlayerManager
import xyz.swagbot.config
import xyz.swagbot.dsl.getRequester
import java.util.concurrent.Executors

/*
 * SwagBot - Created on 8/24/17
 * Author: zachk
 * 
 * stringID code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at the project root.
 */

/**
 * @author zachk
 * @version 8/24/17
 */
private val audioHandlers = mutableMapOf<String, TrackHandler>()

val trackLoader = Executors.newSingleThreadExecutor()!!

fun getAllAudioHandlers(): Map<String, TrackHandler> {
    return audioHandlers
}

fun IGuild.initialize() {
    if (!audioHandlers.contains(stringID)) {
        val player = audioPlayerManager.createPlayer()
        val listener = TrackHandler(this, player)
        player.addListener(listener)

        audioHandlers[stringID] = listener
        audioManager.audioProvider = listener.audioProvider

        val settings = sql {
            sb_guilds.select { sb_guilds.id eq longID }.mapNotNull {
                GuildSettingsLoadObj(
                        it[sb_guilds.id].toLong(),
                        it[sb_guilds.volume],
                        it[sb_guilds.loop],
                        it[sb_guilds.last_voice_channel]?.toLong()
                )
            }.firstOrNull()
        }
        if (settings == null) {
            xyz.swagbot.database.logger.info("Adding new guild to database: $stringID")
            sql {
                sb_guilds.insert {
                    it[sb_guilds.id] = longID
                    it[sb_guilds.name] = this@initialize.name
                    it[sb_guilds.command_prefix] = config.getString("defaults.command_prefix")
                    it[sb_guilds.timezone] = "EST"
                }
            }

            player.volume = 50
        } else {
            player.volume = settings.volume
            if (settings.loop)
                listener.toggleShouldLoop()
        }

        trackLoader.submit { loadTracksFromStorage() }
    }
}

fun IGuild.shutdownAudioPlayer() {
    val toDestroy = audioHandlers.remove(stringID) ?: return
    saveTracksToStorage()
    toDestroy.player.destroy()
}

fun IGuild.getAudioHandler(): TrackHandler {
    val handler = audioHandlers[stringID]!!
    if (!handler.player.isPaused && handler.player.playingTrack == null && handler.getQueue().isNotEmpty())
        handler.playNext()
    return handler
}

fun IGuild.getCommandPrefix(): String {
    return sql { sb_guilds.select { sb_guilds.id eq longID }.firstOrNull()?.get(sb_guilds.command_prefix) ?: getDefault("command_prefix") }
}

fun IGuild.setCommandPrefix(prefix: String) {
    sql { sb_guilds.update({ sb_guilds.id eq longID }) { it[sb_guilds.command_prefix] = prefix } }
}

fun IGuild.getBotVolume(): Int {
    return sql { sb_guilds.select { sb_guilds.id eq longID }.firstOrNull()?.get(sb_guilds.volume) ?: 50 }
}

fun IGuild.setBotVolume(volume: Int) {
    sql { sb_guilds.update({ sb_guilds.id eq longID }) { it[sb_guilds.volume] = volume } }
    getAudioHandler().player.volume = volume
}

fun IGuild.isBotLocked(): Boolean {
    return sql { sb_guilds.select { sb_guilds.id eq longID }.firstOrNull()?.get(sb_guilds.locked) ?: false }
}

fun IGuild.setAutoAssignRole(role: IRole?) {
    sql { sb_guilds.update({ sb_guilds.id eq longID }) { it[sb_guilds.auto_assign_role] = role?.longID } }
}

fun IGuild.getAutoAssignRole(): IRole? {
    val roleId = sql { sb_guilds.select { sb_guilds.id eq longID }.firstOrNull()?.get(sb_guilds.auto_assign_role) } ?: 0
    return try {
        getRoleByID(roleId)
    } catch (t: Throwable) {
        sql { sb_guilds.update({ sb_guilds.id eq longID }) { it[sb_guilds.auto_assign_role] = null } }
        null
    }
}

fun IGuild.setLastVoiceChannel(channel: IVoiceChannel?) {
    sql { sb_guilds.update({ sb_guilds.id eq longID }) { it[sb_guilds.last_voice_channel] = channel?.longID } }
}

fun IGuild.getLastVoiceChannel(): IVoiceChannel? {
    return getVoiceChannelByID(
            sql { sb_guilds.select { sb_guilds.id eq longID }.firstOrNull()?.get(sb_guilds.last_voice_channel) } ?: 0
    )
}

fun IGuild.saveTracksToStorage() {
    val audioHandler = getAudioHandler()
    sql {
        if (audioHandler.player.playingTrack?.identifier != null)
            sb_track_storage.insert {
                it[sb_track_storage.guild_id] = longID
                it[sb_track_storage.user_id] = audioHandler.player.playingTrack.getRequester().longID
                it[sb_track_storage.identifier] = audioHandler.player.playingTrack.info.uri
            }
        sb_track_storage.batchInsert(audioHandler.getQueue()) {
            if (it.identifier != null) {
                this[sb_track_storage.guild_id] = longID
                this[sb_track_storage.user_id] = it.getRequester().longID
                this[sb_track_storage.identifier] = it.info.uri
            }
        }
    }
}

fun IGuild.loadTracksFromStorage() {
    val list = sql {
        sb_track_storage.select { sb_track_storage.guild_id eq longID }
                .map { Pair(it[sb_track_storage.user_id], it[sb_track_storage.identifier]) }
    }

    val audioHandler = getAudioHandler()
    list.forEach {
        audioPlayerManager.loadItemOrdered(
                audioHandler,
                it.second,
                SilentAudioTrackLoadHandler(audioHandler, client.getUserByID(it.first))
        )
    }
}

fun IGuild.isQueueLoopEnabled(): Boolean {
    return getAudioHandler().shouldLoop
}

fun IGuild.toggleQueueLoop(): Boolean {
    val new = getAudioHandler().toggleShouldLoop()
    sql { sb_guilds.update({ sb_guilds.id eq longID }) { it[sb_guilds.loop] = new } }
    return new
}

/*fun IGuild.addChatChannel(channel: IChannel): Boolean {
    return create_chat_channel_entry(stringID, channel.stringID)
}

fun IGuild.removeChatChannel(channel: IChannel): Boolean {
    return remove_chat_channel_entry(channel.stringID)
}

fun IGuild.getChatChannels(): List<IChannel> {
    return get_chat_channels_for_guild(stringID).map { client.getChannelByID(it.toLong()) }
}*/

fun IGuild.getIAmRoleList(): List<IRole> {
    return sql { sb_iam_roles.selectAll().mapNotNull { getRoleByID(it[sb_iam_roles.role_id]) } }
}

fun IGuild.isRoleSelfAssignable(role: IRole): Boolean {
    return sql { sb_iam_roles.select { sb_iam_roles.role_id eq role.longID }.firstOrNull() != null }
}

fun IGuild.removeIAmRole(role: IRole): Boolean {
    return sql {
        if (sb_iam_roles.select { sb_iam_roles.role_id eq role.longID }.firstOrNull() == null)
            return@sql false
        sb_iam_roles.deleteWhere { sb_iam_roles.role_id eq role.longID }
        return@sql true
    }
}

fun IGuild.addIAmRole(role: IRole): Boolean {
    return sql {
        if (sb_iam_roles.select { sb_iam_roles.role_id eq role.longID }.firstOrNull() != null)
            return@sql false
        sb_iam_roles.insert { it[sb_iam_roles.role_id] = role.longID }
        return@sql true
    }
}

fun IGuild.isGameSwitcherEnabled(): Boolean {
    return sql { sb_guilds.select { sb_guilds.id eq longID }.first()[sb_guilds.game_switcher] }
}

fun IGuild.setGameSwitcher(enabled: Boolean) {
    sql { sb_guilds.update({ sb_guilds.id eq longID }) { it[sb_guilds.game_switcher] = enabled } }
}

fun IGuild.addGameSwitcherEntry(game: String, voiceChannel: IVoiceChannel) {
    sql {
        if (sb_game_switcher.select { (sb_game_switcher.guild_id eq longID) and (sb_game_switcher.game eq game) }.firstOrNull() == null) {
            sb_game_switcher.insert {
                it[sb_game_switcher.guild_id] = longID
                it[sb_game_switcher.game] = game
                it[sb_game_switcher.channel_id] = voiceChannel.longID
            }
        } else {
            sb_game_switcher.update({ (sb_game_switcher.guild_id eq longID) and (sb_game_switcher.game eq game) }) {
                it[sb_game_switcher.channel_id] = voiceChannel.longID
            }
        }
        return@sql
    }
}

fun IGuild.removeGameSwitcherEntry(game: String): Map.Entry<String, IVoiceChannel>? {
    return sql {
        val map = mutableMapOf<String, IVoiceChannel>()
        val row = sb_game_switcher.select { (sb_game_switcher.guild_id eq longID) and (sb_game_switcher.game eq game) }.firstOrNull()
        if (row != null) {
            map[game] = getVoiceChannelByID(row[sb_game_switcher.channel_id].toLong())
            sb_game_switcher.deleteWhere { (sb_game_switcher.guild_id eq longID) and (sb_game_switcher.game eq game) }
        }

        return@sql map.entries.firstOrNull()
    }
}

fun IGuild.getGameSwitcherEntries(): Map<String, IVoiceChannel> {
    return sql { sb_game_switcher.select { sb_game_switcher.guild_id eq longID }
            .associate { Pair(it[sb_game_switcher.game], getVoiceChannelByID(it[sb_game_switcher.channel_id].toLong())) } }
}
