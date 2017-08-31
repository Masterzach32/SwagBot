package xyz.swagbot.database

import net.masterzach32.commands4k.Permission
import sx.blah.discord.handle.obj.IGuild
import sx.blah.discord.handle.obj.IRole
import sx.blah.discord.handle.obj.IUser

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
fun IGuild.getCommandPrefix(): String {
    return get_guild_row(stringID)[sb_guilds.command_prefix]
}

fun IGuild.getBotVolume(): Int {
    return get_guild_row(stringID)[sb_guilds.volume]
}

fun IGuild.isBotLocked(): Boolean {
    return get_guild_row(stringID)[sb_guilds.locked]
}

fun IGuild.getUserPermission(user: IUser): Permission {
    return when (get_permission_entry(this, user)) {
        0 -> Permission.NONE
        1 -> Permission.NORMAL
        2 -> Permission.MOD
        3 -> Permission.ADMIN
        4 -> Permission.DEVELOPER
        else -> Permission.NORMAL
    }
}

fun IGuild.setUserPermission(user: IUser, permission: Permission) {
    if (permission == Permission.NORMAL)
        remove_permission_entry(this, user)
    else if (does_user_have_permission_entry(this, user))
        update_permission_entry(this, user, permission)
    else
        create_permission_entry(this, user, permission)
}

fun IGuild.setAutoAssignRole(role: IRole?) {
    set_aar(this, role)
}

fun IGuild.getAutoAssignRole(): IRole? {
    return get_aar(this)
}