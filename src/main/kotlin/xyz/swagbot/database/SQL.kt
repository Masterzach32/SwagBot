package xyz.swagbot.database

import org.jetbrains.exposed.sql.*
import sx.blah.discord.handle.obj.IGuild

/*
 * SwagBot - Created on 8/29/2017
 * Author: Zach Kozar
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at the project root.
 */

/**
 * A class containing low level sql code
 *
 * @author Zach Kozar
 * @version 8/29/2017
 */
internal fun create_table(table: Table) {
    sql {
        create(table)
        commit()
    }
}

internal fun create_guild_entry(guild: IGuild) {
    sql {
        sb_guilds.insert {
            it[sb_guilds.id] = guild.stringID
            it[sb_guilds.name] = guild.name
            it[sb_guilds.command_prefix] = "~"
            it[sb_guilds.volume] = 50
            it[sb_guilds.locked] = false
        }
        commit()
    }
}

internal fun does_guild_entry_exist(id: String): Boolean {
    return get_row(sb_guilds) { sb_guilds.id eq id } != null
}

private fun get_guild_entry(id: String): ResultRow? {
    return get_row(sb_guilds) { sb_guilds.id eq id }
}

internal fun <T> get_guild_cell(id: String, column: Column<T>): T? {
    return get_cell(sb_guilds, column) { sb_guilds.id eq id }
}

internal fun <T> update_guild_cell(id: String, column: Column<T>, value: T?) {
    sql {
        sb_guilds.update({sb_guilds.id eq id}) {
            it[column] = value
        }
        commit()
    }
}

internal fun create_permission_entry(guildId: String, userId: String, permission: Int) {
    sql {
        sb_permissions.insert {
            it[sb_permissions.guild_id] = guildId
            it[sb_permissions.user_id] = userId
            it[sb_permissions.permission] = permission
        }
        commit()
    }
}

internal fun does_user_have_permission_entry(guildId: String, userId: String): Boolean {
    return get_row(sb_permissions)
        { (sb_permissions.guild_id eq guildId) and (sb_permissions.user_id eq userId) } != null
}

internal fun update_permission_entry(guildId: String, userId: String, permission: Int) {
    sql {
        sb_permissions
                .update({(sb_permissions.guild_id eq guildId) and (sb_permissions.user_id eq userId)}) {
                    it[sb_permissions.permission] = permission
                }
        commit()
    }
}

internal fun get_permission_entry(guildId: String, userId: String): Int {
    if (does_user_have_permission_entry(guildId, userId))
        return get_cell(sb_permissions, sb_permissions.permission)
            { (sb_permissions.guild_id eq guildId) and (sb_permissions.user_id eq userId) }!!
    return 1
}

internal fun remove_permission_entry(guildId: String, userId: String) {
    sql {
        sb_permissions.deleteWhere { (sb_permissions.guild_id eq guildId) and (sb_permissions.user_id eq userId) }
        commit()
    }
}

internal fun create_track_entry(guildId: String, userId: String, identifier: String) {
    sql {
        sb_track_storage.insert {
            it[sb_track_storage.guild_id] = guildId
            it[sb_track_storage.user_id] = userId
            it[sb_track_storage.identifier] = identifier
        }
        commit()
    }
}

internal fun remove_track_entries(guildId: String): List<ResultRow> {
    return sql {
        val list = mutableListOf<ResultRow>()
        sb_track_storage.select { sb_track_storage.guild_id eq guildId }.forEach { list.add(it) }
        sb_track_storage.deleteWhere { sb_track_storage.guild_id eq guildId }
        return@sql list
    }
}

internal fun create_chat_channel_entry(guildId: String, channelId: String): Boolean {
    if (has_chat_channel_entry(channelId))
        return false
    sql {
        sb_chat_channels.insert {
            it[sb_chat_channels.guild_id] = guildId
            it[sb_chat_channels.channel_id] = channelId
        }
        commit()
    }
    return true
}

internal fun has_chat_channel_entry(channelId: String): Boolean {
    return sql { return@sql sb_chat_channels.select { sb_chat_channels.channel_id eq channelId }.firstOrNull() != null }
}

internal fun remove_chat_channel_entry(channelId: String): Boolean {
    if (!has_chat_channel_entry(channelId))
        return false
    sql { sb_chat_channels.deleteWhere { sb_chat_channels.channel_id eq channelId } }
    return true
}

internal fun get_chat_channels_for_guild(guildId: String): Set<String> {
    val set = mutableSetOf<String>()
    sql {
        sb_chat_channels
                .select { sb_chat_channels.guild_id eq guildId }
                .forEach { set.add(it[sb_chat_channels.channel_id]) }
    }
    return set
}

internal fun create_iam_role_entry(guildId: String, roleId: String) {
    sql {
        sb_iam_roles.insert {
            it[sb_iam_roles.guild_id] = guildId
            it[sb_iam_roles.role_id] = roleId
        }
        commit()
    }
}

internal fun has_iam_role_entry(roleId: String): Boolean {
    return sql {
        return@sql sb_iam_roles.select { sb_iam_roles.role_id eq roleId }.firstOrNull() != null
    }
}

internal fun get_iam_role_list(guildId: String): List<String> {
    val list = mutableListOf<String>()
    sql {
        sb_iam_roles
                .select { sb_iam_roles.guild_id eq guildId }
                .forEach { list.add(it[sb_iam_roles.role_id]) }
    }
    return list
}

internal fun remove_iam_role_entry(roleId: String) {
    sql { sb_iam_roles.deleteWhere { sb_iam_roles.role_id eq roleId } }
}

internal fun create_music_profile_entry(userId: String, trackIdentifier: String) {
    sql {
        sb_music_profile.insert {
            it[sb_music_profile.user_id] = userId
            it[sb_music_profile.identifier] = trackIdentifier
            it[sb_music_profile.count] = 1
        }
        commit()
    }
}

internal fun get_music_profile_count(userId: String, trackIdentifier: String): Int {
    return sql {
        val result = sb_music_profile
                .select { (sb_music_profile.user_id eq userId) and (sb_music_profile.identifier eq trackIdentifier) }
                .firstOrNull() ?: return@sql 0
        return@sql result[sb_music_profile.count]
    }
}

internal fun change_music_profile_count(userId: String, trackIdentifier: String, count: Int) {
    sql {
        sb_music_profile
                .update({(sb_music_profile.user_id eq userId) and (sb_music_profile.identifier eq trackIdentifier)}) {
                    it[sb_music_profile.count] = count
                }
        commit()
    }
}

/*
    Generic SQL code
 */

private fun get_row(table: Table, where: SqlExpressionBuilder.() -> Op<Boolean>): ResultRow? {
    return sql { return@sql table.select(where).firstOrNull() }
}

private fun <T> get_cell(row: ResultRow, column: Column<T>): T? {
    return sql { return@sql row[column] }
}

internal fun <T> get_cell(table: Table, column: Column<T>, where: SqlExpressionBuilder.() -> Op<Boolean>): T? {
    return sql { return@sql table.select(where).firstOrNull()?.get(column) }
}

private fun <T> update_cell(table: Table, column: Column<T>, where: SqlExpressionBuilder.() -> Op<Boolean>, value: T) {
    sql { table.update(where) { it[column] = value } }
}

internal fun get_row_count(table: Table): Int {
    return sql { return@sql table.selectAll().count() }
}