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
    }
}

internal fun get_permission_entry(guildId: String, userId: String): Int {
    if (does_user_have_permission_entry(guildId, userId))
        return get_cell(sb_permissions, sb_permissions.permission)
            { (sb_permissions.guild_id eq guildId) and (sb_permissions.user_id eq userId) }!!
    return 1
}

internal fun remove_permission_entry(guildId: String, userId: String) {
    sql { sb_permissions.deleteWhere { (sb_permissions.guild_id eq guildId) and (sb_permissions.user_id eq userId) } }
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