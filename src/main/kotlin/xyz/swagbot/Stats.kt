package xyz.swagbot

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import xyz.swagbot.database.sb_stats
import xyz.swagbot.database.sql

/*
 * SwagBot - Created on 6/12/2018
 * Author: Zach Kozar
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at the project root.
 */

/**
 * @author Zach Kozar
 * @version 6/12/2018
 */
object Stats {

    private val stats = mutableListOf<Stat>()

    val COMMANDS_USED = Stat("cmds_used")
    val TRACKS_PLAYED = Stat("tracks_played")
    val TRACKS_SKIPPED = Stat("tracks_skipped")
    val TRACK_SKIP_VOTES = Stat("track_skip_votes")
    val TRACKS_AUTOPLAYED = Stat("tracks_auto")
    val ROLES_ASSIGNED = Stat("roles_assigned")
    val MESSAGES_PRUNED = Stat("messages_pruned")
    val GAMES_PLAYED = Stat("bot_games_played")
    val CATS_FETCHED = Stat("cat_img")
    val DOGS_FETCHED = Stat("dog_img")
    val GAMESWITCHER_USERS_MOVED = Stat("gameswitcher_users_moved")

    init {
        stats.filter { !it.exists() }.forEach { it.create() }
        stats.forEach { it.read() }
    }

    class Stat internal constructor(private val name: String) {

        private var stat: Int = 0
            set(value) {
                field = value
                sql { sb_stats.update({ sb_stats.key eq name }) { it[sb_stats.value] = field } }
            }

        init {
            stats.add(this)
        }

        fun addStat() = addStat(1)

        fun addStat(amount: Int) {
            stat += amount
        }

        internal fun exists(): Boolean = sql { sb_stats.select { sb_stats.key eq name }.firstOrNull() != null }

        internal fun create() = sql { sb_stats.insert { it[sb_stats.key] = name } }

        internal fun read() {
            stat = sql { sb_stats.select { sb_stats.key eq name }.first()[sb_stats.value] }
        }
    }
}