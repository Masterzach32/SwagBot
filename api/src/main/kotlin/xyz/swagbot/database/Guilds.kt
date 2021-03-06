package xyz.swagbot.database

import org.jetbrains.exposed.sql.*
import sx.blah.discord.handle.obj.*
import xyz.swagbot.DEFAULT_COMMAND_PREFIX
import xyz.swagbot.api.music.SilentAudioTrackLoadHandler
import xyz.swagbot.api.music.TrackHandler
import xyz.swagbot.audioPlayerManager
import xyz.swagbot.dsl.getConnectedVoiceChannel
import xyz.swagbot.dsl.getRequester
import xyz.swagbot.dsl.request
import xyz.swagbot.dsl.requestGet
import xyz.swagbot.logger

/*
 * SwagBot - Created on 8/24/17
 * Author: zachk
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at the project root.
 */

/**
 * @author zachk
 * @version 8/24/17
 */
private val audioHandlers = mutableMapOf<Long, TrackHandler>()

fun getAllAudioHandlers(): Map<Long, TrackHandler> = audioHandlers

fun IGuild.initialize() {
    if (!audioHandlers.contains(longID)) {
        val listener = createPlayer()

        val settings = sql {
            Guilds.select { Guilds.id eq longID }.mapNotNull {
                GuildSettingsLoadObj(
                        it[Guilds.id].toLong(),
                        it[Guilds.volume],
                        it[Guilds.loop],
                        it[Guilds.last_voice_channel]?.toLong()
                )
            }.firstOrNull()
        }
        if (settings == null) {
            logger.info("Adding new guild to database: $name ($longID)")
            sql {
                Guilds.insert {
                    it[Guilds.id] = longID
                    it[Guilds.name] = this@initialize.name
                    it[Guilds.command_prefix] = DEFAULT_COMMAND_PREFIX
                    it[Guilds.timezone] = "EST"
                }
            }

            listener.player.volume = 50
        } else {
            listener.player.volume = settings.volume
            if (settings.loop)
                listener.toggleShouldLoop()
        }

        loadTracksFromStorage()
    }
}

fun IGuild.shutdownAudioPlayer(saveTracks: Boolean) {
    if (saveTracks)
        saveTracksToStorage()
    audioHandlers.remove(longID)?.player?.destroy()
}

fun IGuild.refreshAudioPlayer() {
    shutdownAudioPlayer(false)
    client.ourUser.getConnectedVoiceChannel(this)?.leave()
    initialize()
}

private fun IGuild.createPlayer(): TrackHandler {
    val player = audioPlayerManager.createPlayer()
    val listener = TrackHandler(this, player)
    player.addListener(listener)

    audioHandlers[longID] = listener
    audioManager.audioProvider = listener.audioProvider
    return listener
}

val IGuild.trackHandler: TrackHandler
    get() {
        if (audioHandlers[longID] == null)
            initialize()
        val handler = audioHandlers[longID]!!
        if (!handler.player.isPaused && handler.player.playingTrack == null && handler.getQueue().isNotEmpty())
            handler.playNext()
        return handler
    }

var IGuild.commandPrefix: String
    get() = sql { Guilds.select { Guilds.id eq longID }.firstOrNull()?.get(Guilds.command_prefix) ?: DEFAULT_COMMAND_PREFIX }
    set(value) = sql { Guilds.update({ Guilds.id eq longID }) { it[Guilds.command_prefix] = value } }

var IGuild.botVolume: Int
    get() = sql { Guilds.select { Guilds.id eq longID }.firstOrNull()?.get(Guilds.volume) ?: 50 }
    set(value) {
        sql { Guilds.update({ Guilds.id eq longID }) { it[Guilds.volume] = value } }
        trackHandler.player.volume = value
    }

var IGuild.isBotLocked: Boolean
    get() = sql { Guilds.select { Guilds.id eq longID }.firstOrNull()?.get(Guilds.locked) ?: false }
    set(value) = sql { Guilds.update({ Guilds.id eq longID }) { it[Guilds.locked] = value } }

var IGuild.autoAssignRole: IRole?
    get() {
        val roleId = sql { Guilds.select { Guilds.id eq longID }.firstOrNull()?.get(Guilds.auto_assign_role) } ?: ""
        return try {
            getRoleByID(roleId.toLong())
        } catch (t: Throwable) {
            getRolesByName(roleId).firstOrNull()
            //sql { Guilds.update({ Guilds.id eq longID }) { it[Guilds.auto_assign_role] = null } }
        }
    }
    set(value) = sql { Guilds.update({ Guilds.id eq longID }) { it[Guilds.auto_assign_role] = value?.stringID } }

var IGuild.lastVoiceChannel: IVoiceChannel?
    get() = getVoiceChannelByID(sql { Guilds.select { Guilds.id eq longID }.firstOrNull()?.get(Guilds.last_voice_channel) } ?: 0)
    set(value) = sql { Guilds.update({ Guilds.id eq longID }) { it[Guilds.last_voice_channel] = value?.longID } }

fun IGuild.saveTracksToStorage() {
    val audioHandler = trackHandler
    sql {
        if (audioHandler.player.playingTrack?.identifier != null)
            TrackStorage.insert {
                it[TrackStorage.guild_id] = longID
                it[TrackStorage.user_id] = audioHandler.player.playingTrack.getRequester().longID
                it[TrackStorage.identifier] = audioHandler.player.playingTrack.info.uri
            }
        TrackStorage.batchInsert(audioHandler.getQueue()) {
            if (it.identifier != null) {
                this[TrackStorage.guild_id] = longID
                this[TrackStorage.user_id] = it.getRequester().longID
                this[TrackStorage.identifier] = it.info.uri
            }
        }
    }
}

fun IGuild.loadTracksFromStorage() {
    val list = sql {
        TrackStorage.select { TrackStorage.guild_id eq longID }
                .map { it[TrackStorage.user_id] to it[TrackStorage.identifier] }
    }

    list.forEach {
        audioPlayerManager.loadItemOrdered(
                trackHandler,
                it.second,
                SilentAudioTrackLoadHandler(trackHandler, client.getUserByID(it.first))
        )
    }
}

fun IGuild.isQueueLoopEnabled(): Boolean {
    return trackHandler.shouldLoop
}

fun IGuild.toggleQueueLoop(): Boolean {
    val new = trackHandler.toggleShouldLoop()
    sql { Guilds.update({ Guilds.id eq longID }) { it[Guilds.loop] = new } }
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
    return sql { IAmRoles.selectAll().mapNotNull { getRoleByID(it[IAmRoles.role_id]) } }
}

fun IGuild.isRoleSelfAssignable(role: IRole): Boolean {
    return sql { IAmRoles.select { IAmRoles.role_id eq role.longID }.firstOrNull() != null }
}

fun IGuild.removeIAmRole(role: IRole): Boolean {
    return sql {
        if (IAmRoles.select { IAmRoles.role_id eq role.longID }.firstOrNull() == null)
            return@sql false
        IAmRoles.deleteWhere { IAmRoles.role_id eq role.longID }
        return@sql true
    }
}

fun IGuild.addIAmRole(role: IRole): Boolean {
    return sql {
        if (IAmRoles.select { IAmRoles.role_id eq role.longID }.firstOrNull() != null)
            return@sql false
        IAmRoles.insert { it[IAmRoles.role_id] = role.longID }
        return@sql true
    }
}

fun IGuild.isGameSwitcherEnabled(): Boolean {
    return sql { Guilds.select { Guilds.id eq longID }.first()[Guilds.game_switcher] }
}

fun IGuild.setGameSwitcher(enabled: Boolean) {
    sql { Guilds.update({ Guilds.id eq longID }) { it[Guilds.game_switcher] = enabled } }
}

fun IGuild.addGameSwitcherEntry(game: String, voiceChannel: IVoiceChannel) {
    sql {
        if (GameSwitcher.select { (GameSwitcher.guild_id eq longID) and (GameSwitcher.game eq game) }.firstOrNull() == null) {
            GameSwitcher.insert {
                it[GameSwitcher.guild_id] = longID
                it[GameSwitcher.game] = game
                it[GameSwitcher.channel_id] = voiceChannel.longID
            }
        } else {
            GameSwitcher.update({ (GameSwitcher.guild_id eq longID) and (GameSwitcher.game eq game) }) {
                it[GameSwitcher.channel_id] = voiceChannel.longID
            }
        }
        return@sql
    }
}

fun IGuild.removeGameSwitcherEntry(game: String): Map.Entry<String, IVoiceChannel>? {
    return sql {
        val map = mutableMapOf<String, IVoiceChannel>()
        val row = GameSwitcher.select { (GameSwitcher.guild_id eq longID) and (GameSwitcher.game eq game) }.firstOrNull()
        if (row != null) {
            map[game] = getVoiceChannelByID(row[GameSwitcher.channel_id].toLong())
            GameSwitcher.deleteWhere { (GameSwitcher.guild_id eq longID) and (GameSwitcher.game eq game) }
        }

        return@sql map.entries.firstOrNull()
    }
}

fun IGuild.getGameSwitcherEntries(): Map<String, IVoiceChannel> {
    return sql {
        GameSwitcher.select { GameSwitcher.guild_id eq longID }
            .associate { it[GameSwitcher.game] to getVoiceChannelByID(it[GameSwitcher.channel_id].toLong()) }
    }
}

val IGuild.areTempChannelsEnabled: Boolean
    get() = tempChannelCategory != null

var IGuild.tempChannelCategory: ICategory?
    get() = sql { Guilds.select(thisGuild()).first()[Guilds.temp_category]?.let { client.getCategoryByID(it) } }
    set(value) = sql { Guilds.update(thisGuild()) { it[Guilds.temp_category] = value?.longID } }

fun IGuild.createTempChannel(name: String, owner: IUser): IVoiceChannel {
    return createVoiceChannel(name).also { channel ->
        sql {
            TempChannel.new {
                guildId = longID
                channelId = channel.longID
                ownerId = owner.longID
            }
        }
    }
}

fun IGuild.getTempChannelForUser(user: IUser) = sql {
    TempChannel.find { TempChannels.user_id eq user.longID }.firstOrNull()?.let { client.getVoiceChannelByID(it.channelId) }
}

fun IGuild.getTempChannels() = sql {
    TempChannel.find { TempChannels.guild_id eq longID }
            .mapNotNull { requestGet { client.getVoiceChannelByID(it.channelId) } }
}


// make sql easier

private fun IGuild.thisGuild(): SqlExpressionBuilder.() -> Op<Boolean> = { Guilds.id eq longID }