package xyz.swagbot.database

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.masterzach32.commands4k.Permission
import org.jetbrains.exposed.sql.select
import sx.blah.discord.handle.obj.IGuild
import sx.blah.discord.handle.obj.IUser

fun IUser.getBotPermission(guild: IGuild): Permission {
    return when (get_permission_entry(guild.stringID, stringID)) {
        0 -> Permission.NONE
        1 -> Permission.NORMAL
        2 -> Permission.MOD
        3 -> Permission.ADMIN
        4 -> Permission.DEVELOPER
        else -> Permission.NORMAL
    }
}

fun IUser.setBotPermission(guild: IGuild, permission: Permission) {
    if (permission == Permission.NORMAL)
        remove_permission_entry(guild.stringID, stringID)
    else if (does_user_have_permission_entry(guild.stringID, stringID))
        update_permission_entry(guild.stringID, stringID, permission.ordinal)
    else
        create_permission_entry(guild.stringID, stringID, permission.ordinal)
}

fun IUser.getBotDMPermission(): Permission {
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

fun IUser.addTrackToDatabase(track: AudioTrack) {
    val count = get_music_profile_count(stringID, track.info.uri)

    if (count == 0)
        create_music_profile_entry(stringID, track.info.uri)
    else
        change_music_profile_count(stringID, track.info.uri, count + 1)
}