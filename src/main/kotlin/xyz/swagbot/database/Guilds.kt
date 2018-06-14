package xyz.swagbot.database

import org.jetbrains.exposed.sql.*
import sx.blah.discord.handle.obj.*
import xyz.swagbot.api.music.SilentAudioTrackLoadHandler
import xyz.swagbot.api.music.TrackHandler
import xyz.swagbot.audioPlayerManager
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

private val trackLoader = Executors.newSingleThreadExecutor()

fun getAllAudioHandlers(): Map<String, TrackHandler> {
    return audioHandlers
}

fun IGuild.initialize(settings: GuildSettingsLoadObj?) {
    if (!audioHandlers.contains(stringID)) {
        val player = audioPlayerManager.createPlayer()
        val listener = TrackHandler(this, player)
        player.addListener(listener)

        audioHandlers[stringID] = listener
        audioManager.audioProvider = listener.audioProvider

        if (settings == null) {
            xyz.swagbot.database.logger.info("Adding new guild to database: $stringID")
            create_guild_entry(this)

            player.volume = 50
        } else {
            player.volume = settings.volume
            if (settings.loop)
                listener.toggleShouldLoop()
        }

        trackLoader.submit { listener.loadTracksFromStorage(this) }
    }
}

fun IGuild.shutdownAudioPlayer() {
    val toDestroy = audioHandlers.remove(stringID) ?: return
    toDestroy.saveTracksToStorage(this)
    toDestroy.player.destroy()
}

fun IGuild.getAudioHandler(): TrackHandler {
    val handler = audioHandlers[stringID]!!
    if (!handler.player.isPaused && handler.player.playingTrack == null && handler.getQueue().isNotEmpty())
        handler.playNext()
    return handler
}

fun IGuild.getCommandPrefix(): String {
    return get_guild_cell(stringID, sb_guilds.command_prefix)!!
}

fun IGuild.setCommandPrefix(prefix: String) {
    update_guild_cell(stringID, sb_guilds.command_prefix, prefix)
}

fun IGuild.getBotVolume(): Int {
    return get_guild_cell(stringID, sb_guilds.volume)!!
}

fun IGuild.setBotVolume(volume: Int) {
    update_guild_cell(stringID, sb_guilds.volume, volume)
    getAudioHandler().player.volume = volume
}

fun IGuild.isBotLocked(): Boolean {
    return get_guild_cell(stringID, sb_guilds.locked)!!
}

fun IGuild.setAutoAssignRole(role: IRole?) {
    update_guild_cell(stringID, sb_guilds.auto_assign_role, role?.stringID)
}

fun IGuild.getAutoAssignRole(): IRole? {
    val roleId = get_guild_cell(stringID, sb_guilds.auto_assign_role) ?: return null
    return try {
        getRoleByID(roleId.toLong())
    } catch (t: Throwable) {
        getRolesByName(roleId).firstOrNull()
    }
}

fun IGuild.setLastVoiceChannel(channel: IVoiceChannel?) {
    update_guild_cell(stringID, sb_guilds.last_voice_channel, channel?.stringID)
}

fun IGuild.getLastVoiceChannel(): IVoiceChannel? {
    return getVoiceChannelByID(get_guild_cell(stringID, sb_guilds.last_voice_channel)?.toLong() ?: 0)
}

fun TrackHandler.saveTracksToStorage(guild: IGuild) {
    if (player.playingTrack != null && player.playingTrack.identifier != null)
        create_track_entry(guild.stringID, player.playingTrack.getRequester().stringID, player.playingTrack.info.uri)
    getQueue().forEach {
        if (it.identifier != null)
            create_track_entry(guild.stringID, it.getRequester().stringID, it.info.uri)
    }
    sql { commit() }
}

fun TrackHandler.loadTracksFromStorage(guild: IGuild) {
    remove_track_entries(guild.stringID).forEach {
        audioPlayerManager.loadItemOrdered(this, it[sb_track_storage.identifier],
                SilentAudioTrackLoadHandler(this,
                        guild.client.getUserByID(it[sb_track_storage.user_id].toLong())))
    }
}

fun IGuild.isQueueLoopEnabled(): Boolean {
    return get_guild_cell(stringID, sb_guilds.loop) ?: false
}

fun IGuild.toggleQueueLoop(): Boolean {
    val new = getAudioHandler().toggleShouldLoop()
    update_guild_cell(stringID, sb_guilds.loop, new)
    return new
}

fun IGuild.addChatChannel(channel: IChannel): Boolean {
    return create_chat_channel_entry(stringID, channel.stringID)
}

fun IGuild.removeChatChannel(channel: IChannel): Boolean {
    return remove_chat_channel_entry(channel.stringID)
}

fun IGuild.getChatChannels(): List<IChannel> {
    return get_chat_channels_for_guild(stringID).map { client.getChannelByID(it.toLong()) }
}

fun IGuild.getIAmRoleList(): List<IRole?> {
    return get_iam_role_list(stringID).map { getRoleByID(it.toLong()) }
}

fun IGuild.isRoleSelfAssignable(role: IRole): Boolean {
    return has_iam_role_entry(role.stringID)
}

fun IGuild.removeIAmRole(role: IRole): Boolean {
    if (!has_iam_role_entry(role.stringID))
        return false
    remove_iam_role_entry(role.stringID)
    return true
}

fun IGuild.addIAmRole(role: IRole): Boolean {
    if (has_iam_role_entry(role.stringID))
        return false
    create_iam_role_entry(stringID, role.stringID)
    return true
}

fun IGuild.isGameSwitcherEnabled(): Boolean {
    return get_guild_cell(stringID, sb_guilds.game_switcher)!!
}

fun IGuild.setGameSwitcher(enabled: Boolean) {
    sql { sb_guilds.update({ sb_guilds.id eq stringID }) { it[sb_guilds.game_switcher] = enabled } }
}

fun IGuild.addGameSwitcherEntry(game: String, voiceChannel: IVoiceChannel) {
    sql {
        if (sb_game_switcher.select { (sb_game_switcher.guild_id eq stringID) and (sb_game_switcher.game eq game) }.firstOrNull() == null) {
            sb_game_switcher.insert {
                it[sb_game_switcher.guild_id] = stringID
                it[sb_game_switcher.game] = game
                it[sb_game_switcher.channel_id] = voiceChannel.stringID
            }
        } else {
            sb_game_switcher.update({ (sb_game_switcher.guild_id eq stringID) and (sb_game_switcher.game eq stringID) }) {
                it[sb_game_switcher.channel_id] = voiceChannel.stringID
            }
        }
        return@sql
    }
}

fun IGuild.removeGameSwitcherEntry(game: String): Map.Entry<String, IVoiceChannel>? {
    return sql {
        val map = mutableMapOf<String, IVoiceChannel>()
        val row = sb_game_switcher.select { (sb_game_switcher.guild_id eq stringID) and (sb_game_switcher.game eq game) }.firstOrNull()
        if (row != null) {
            map[game] = getVoiceChannelByID(row[sb_game_switcher.channel_id].toLong())
            sb_game_switcher.deleteWhere { (sb_game_switcher.guild_id eq stringID) and (sb_game_switcher.game eq game) }
        }

        return@sql map.entries.firstOrNull()
    }
}

fun IGuild.getGameSwitcherEntries(): Map<String, IVoiceChannel> {
    return sql { sb_game_switcher.select { sb_game_switcher.guild_id eq stringID }
            .associate { Pair(it[sb_game_switcher.game], getVoiceChannelByID(it[sb_game_switcher.channel_id].toLong())) } }
}
