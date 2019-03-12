package xyz.swagbot.database

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.masterzach32.commands4k.Permission
import org.jetbrains.exposed.sql.*
import sx.blah.discord.handle.obj.IGuild
import sx.blah.discord.handle.obj.IUser

fun IUser.getBotPermission(guild: IGuild?): Permission {
    if (guild == null)
        return this.getBotDMPermission()
    else {
        val perm = sql {
            Permissions.select { (Permissions.user_id eq longID) and (Permissions.guild_id eq guild.longID) }
                    .firstOrNull()?.get(Permissions.permission) ?: Permission.NORMAL.ordinal
        }.let { Permission.values()[it] }

        if (guild.owner == this && perm != Permission.DEVELOPER)
            return Permission.ADMIN
        else
            return perm
    }
}

fun IUser.setBotPermission(guild: IGuild, permission: Permission) {
    sql<Unit> {
        if (permission == Permission.NORMAL)
            Permissions.deleteWhere { Permissions.guild_id eq guild.longID and (Permissions.user_id eq longID) }
        else if (Permissions.select { Permissions.guild_id eq guild.longID and (Permissions.user_id eq longID) }.firstOrNull() != null)
            Permissions.update({ Permissions.guild_id eq guild.longID and (Permissions.user_id eq longID) }) {
                it[Permissions.permission] = permission.ordinal
            }
        else
            Permissions.insert {
                it[Permissions.guild_id] = guild.longID
                it[Permissions.user_id] = longID
                it[Permissions.permission] = permission.ordinal
            }
    }
}

fun IUser.getBotDMPermission(): Permission {
    return sql {
        val perms = Permissions.select { Permissions.user_id eq longID }
                .map {
                    when (it[Permissions.permission]) {
                        0 -> Permission.NONE
                        1 -> Permission.NORMAL
                        2 -> Permission.MOD
                        3 -> Permission.ADMIN
                        4 -> Permission.DEVELOPER
                        else -> Permission.NORMAL
                    }
                }

        if (perms.contains(Permission.DEVELOPER))
            return@sql Permission.DEVELOPER
        return@sql Permission.NORMAL
    }
}

fun IUser.addTrackToDatabase(track: AudioTrack) {
    sql<Unit> {
        val count = MusicProfile.select { MusicProfile.user_id eq longID and (MusicProfile.identifier eq track.info.uri) }
                .firstOrNull()?.get(MusicProfile.count) ?: 0

        if (count == 0)
            MusicProfile.insert {
                it[MusicProfile.user_id] = longID
                it[MusicProfile.identifier] = track.info.uri
                it[MusicProfile.count] = 1
            }
        else
            MusicProfile.update({ MusicProfile.user_id eq longID and (MusicProfile.identifier eq track.info.uri) }) {
                it[MusicProfile.count] = count + 1
            }
    }
}

fun IUser.getTrackPreferences(): Map<String, Int> {
    return sql {
        return@sql MusicProfile
                .select { MusicProfile.user_id eq longID }
                .associate { it[MusicProfile.identifier] to it[MusicProfile.count] }
    }
}