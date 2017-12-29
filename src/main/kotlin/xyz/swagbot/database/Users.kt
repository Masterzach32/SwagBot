package xyz.swagbot.database

import net.masterzach32.commands4k.Permission
import org.jetbrains.exposed.sql.select
import sx.blah.discord.handle.obj.IUser

fun IUser.getDMPermission(): Permission {
    return sql {
        val perms = mutableListOf<Permission>()
        sb_permissions
                .select { sb_permissions.user_id eq stringID }
                .forEach {
                    perms.add(when (it[sb_permissions.permission]) {
                        0 -> Permission.NONE
                        1 -> Permission.NORMAL
                        2 -> Permission.MOD
                        3 -> Permission.ADMIN
                        4 -> Permission.DEVELOPER
                        else -> Permission.NORMAL
                    })
                }

        if (perms.contains(Permission.DEVELOPER))
            return@sql Permission.DEVELOPER
        return@sql Permission.NORMAL
    }
}