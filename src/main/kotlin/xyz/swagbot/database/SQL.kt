package xyz.swagbot.database

import net.masterzach32.commands4k.Permission
import org.jetbrains.exposed.sql.*
import sx.blah.discord.handle.obj.IGuild
import sx.blah.discord.handle.obj.IRole
import sx.blah.discord.handle.obj.IUser
import sx.blah.discord.handle.obj.IVoiceChannel

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

internal fun does_guild_entry_exist(guild: IGuild): Boolean {
    return sql { return@sql sb_guilds.select { sb_guilds.id eq guild.stringID }.firstOrNull() == null }
}

private fun get_guild_entry(id: String): ResultRow {
    return sql { return@sql sb_guilds.select { sb_guilds.id eq id }.first() }
}

internal fun <T> get_guild_cell(id: String, column: Column<T>): T? {
    return get_guild_entry(id)[column]
}

internal fun <T> update_guild_cell(id: String, column: Column<T>, value: T?) {
    sql {
        sb_guilds.update({sb_guilds.id eq id}) {
            it[column] = value
        }
    }
}

internal fun create_permission_entry(guild: IGuild, user: IUser, permission: Permission) {
    sql {
        sb_permissions.insert {
            it[sb_permissions.guild_id] = guild.stringID
            it[sb_permissions.user_id] = user.stringID
            it[sb_permissions.permission] = permission.ordinal
        }
        commit()
    }
}

internal fun does_user_have_permission_entry(guild: IGuild, user: IUser): Boolean {
    return sql { return@sql sb_permissions.select { (sb_permissions.guild_id eq guild.stringID) and (sb_permissions.user_id eq user.stringID) }.count() == 1 }
}

internal fun update_permission_entry(guild: IGuild, user: IUser, permission: Permission) {
    sql {
        sb_permissions
                .update({(sb_permissions.guild_id eq guild.stringID) and (sb_permissions.user_id eq user.stringID)}) {
                    it[sb_permissions.permission] = permission.ordinal
                }
    }
}

internal fun get_permission_entry(guild: IGuild, user: IUser): Int {
    if (does_user_have_permission_entry(guild, user))
        return sql { return@sql sb_permissions
                .select { (sb_permissions.guild_id eq guild.stringID) and (sb_permissions.user_id eq user.stringID) }
                .first()
        }[sb_permissions.permission]
    return 1
}

internal fun remove_permission_entry(guild: IGuild, user: IUser) {
    sql { sb_permissions.deleteWhere { (sb_permissions.guild_id eq guild.stringID) and (sb_permissions.user_id eq user.stringID) } }
}