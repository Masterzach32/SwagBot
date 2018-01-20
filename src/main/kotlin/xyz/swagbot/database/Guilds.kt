package xyz.swagbot.database

import sx.blah.discord.handle.obj.*
import xyz.swagbot.api.music.SilentAudioTrackLoadHandler
import xyz.swagbot.api.music.TrackHandler
import xyz.swagbot.audioPlayerManager
import xyz.swagbot.dsl.getTrackUserData

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

fun getAllAudioHandlers(): Map<String, TrackHandler> {
    return audioHandlers
}

fun IGuild.initializeAutioPlayer() {
    if (!audioHandlers.contains(stringID)) {
        val player = audioPlayerManager.createPlayer()
        val listener = TrackHandler(player)
        player.addListener(listener)

        audioHandlers.put(stringID, listener)
        player.volume = getBotVolume()
        if (isQueueLoopEnabled())
            listener.toggleShouldLoop()

        listener.loadTracksFromStorage(this)
    }
}

fun IGuild.shutdownAudioPlayer() {
    val toDestroy = audioHandlers.remove(stringID) ?: return
    toDestroy.saveTracksToStorage(this)
    toDestroy.player.destroy()
}

fun IGuild.getAudioHandler(): TrackHandler {
    return audioHandlers[stringID]!!
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
    update_guild_cell(stringID, sb_guilds.auto_assign_role, role?.name)
}

fun IGuild.getAutoAssignRole(): IRole? {
    return getRolesByName(get_guild_cell(stringID, sb_guilds.auto_assign_role)).firstOrNull()
}

fun IGuild.setLastVoiceChannel(channel: IVoiceChannel?) {
    update_guild_cell(stringID, sb_guilds.last_voice_channel, channel?.stringID)
}

fun IGuild.getLastVoiceChannel(): IVoiceChannel? {
    return getVoiceChannelByID(get_guild_cell(stringID, sb_guilds.last_voice_channel)?.toLong() ?: 0)
}

fun TrackHandler.saveTracksToStorage(guild: IGuild) {
    if (player.playingTrack != null && player.playingTrack.identifier != null)
        create_track_entry(guild.stringID, player.playingTrack.getTrackUserData().requester.stringID, player.playingTrack.identifier)
    getQueue().forEach {
        if (it.identifier != null)
            create_track_entry(guild.stringID, it.getTrackUserData().requester.stringID, it.identifier)
    }
    sql { commit() }
}

fun TrackHandler.loadTracksFromStorage(guild: IGuild) {
    remove_track_entries(guild.stringID).forEach {
        audioPlayerManager.loadItemOrdered(this,
                it[sb_track_storage.identifier],
                SilentAudioTrackLoadHandler(this, guild,
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