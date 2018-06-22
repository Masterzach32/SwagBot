package xyz.swagbot.database

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.masterzach32.commands4k.Permission
import org.jetbrains.exposed.sql.*
import sx.blah.discord.handle.obj.IGuild
import sx.blah.discord.handle.obj.IUser

fun IUser.getBotPermission(guild: IGuild): Permission {
    val permId = sql {
        sb_permissions.select { (sb_permissions.user_id eq longID) and (sb_permissions.guild_id eq guild.longID) }
                .firstOrNull()?.get(sb_permissions.permission) ?: Permission.NORMAL
    }
    return when (permId) {
        0 -> Permission.NONE
        1 -> Permission.NORMAL
        2 -> Permission.MOD
        3 -> Permission.ADMIN
        4 -> Permission.DEVELOPER
        else -> Permission.NORMAL
    }
}

fun IUser.setBotPermission(guild: IGuild, permission: Permission) {
    sql<Unit> {
        if (permission == Permission.NORMAL)
            sb_permissions.deleteWhere { sb_permissions.guild_id eq guild.longID and (sb_permissions.user_id eq longID) }
        else if (sb_permissions.select { sb_permissions.guild_id eq guild.longID and (sb_permissions.user_id eq longID) }.firstOrNull() != null)
            sb_permissions.update({ sb_permissions.guild_id eq guild.longID and (sb_permissions.user_id eq longID) }) {
                it[sb_permissions.permission] = permission.ordinal
            }
        else
            sb_permissions.insert {
                it[sb_permissions.guild_id] = guild.longID
                it[sb_permissions.user_id] = longID
                it[sb_permissions.permission] = permission.ordinal
            }
    }
}

fun IUser.getBotDMPermission(): Permission {
    return sql {
        val perms = mutableListOf<Permission>()
        sb_permissions
                .select { sb_permissions.user_id eq longID }
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
    sql<Unit> {
        val count = sb_music_profile.select { sb_music_profile.user_id eq longID and (sb_music_profile.identifier eq track.info.uri) }
                .firstOrNull()?.get(sb_music_profile.count) ?: 0

        if (count == 0)
            sb_music_profile.insert {
                it[sb_music_profile.user_id] = longID
                it[sb_music_profile.identifier] = track.info.uri
                it[sb_music_profile.count] = 1
            }
        else
            sb_music_profile.update({ sb_music_profile.user_id eq longID and (sb_music_profile.identifier eq track.info.uri) }) {
                it[sb_music_profile.count] = count + 1
            }
    }
}

fun IUser.getTrackPreferences(): Map<String, Int> {
    val map = mutableMapOf<String, Int>()

    sql {
        sb_music_profile
                .select { sb_music_profile.user_id eq longID }
                .forEach { map[it[sb_music_profile.identifier]] = it[sb_music_profile.count] }
    }

    return map
}