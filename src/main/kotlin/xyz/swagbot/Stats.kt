package xyz.swagbot

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import xyz.swagbot.database.Stats
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

    val COMMANDS_USED = Stat("Commands Used", "cmds_used")
    val TRACKS_PLAYED = Stat("Tracks Played", "tracks_played")
    val TRACKS_SKIPPED = Stat("Tracks Skipped", "tracks_skipped")
    val TRACK_SKIP_VOTES = Stat("Track Skip Votes", "track_skip_votes")
    val TRACKS_AUTOPLAYED = Stat("Tracks Auto-played", "tracks_auto")
    val ROLES_ASSIGNED = Stat("Roles Auto-assigned", "roles_assigned")
    val MESSAGES_PRUNED = Stat("Messages Pruned", "messages_pruned")
    val GAMES_PLAYED = Stat("Games Played", "bot_games_played")
    val CATS_FETCHED = Stat("Cat Images Fetched", "cat_img")
    val DOGS_FETCHED = Stat("Dog Images Fetched", "dog_img")
    val GAMESWITCHER_USERS_MOVED = Stat("Users Moved by GameSwitcher", "gameswitcher_users_moved")
    val LMGTFY_SEARCH = Stat("LMGTFY Uses", "lmgtfy")
    val STRAWPOLL = Stat("Strawpolls Created", "strawpoll")
    val POLLS_CREATED = Stat("Polls Created", "polls_created")

    init {
        stats.filter { !it.exists() }.forEach { it.create() }
        stats.forEach { it.read() }
    }

    fun getStatObjects(): List<Stat> = stats

    class Stat internal constructor(val name: String, private val identifier: String) {

        var stat: Int = 0
            private set(value) {
                field = value
                sql { Stats.update({ Stats.key eq identifier }) { it[Stats.value] = field } }
            }

        init {
            stats.add(this)
        }

        fun addStat() = addStat(1)

        fun addStat(amount: Int) {
            stat += amount
        }

        internal fun exists(): Boolean = sql { Stats.select { Stats.key eq identifier }.firstOrNull() != null }

        internal fun create() = sql { Stats.insert { it[Stats.key] = identifier } }

        internal fun read() {
            stat = sql { Stats.select { Stats.key eq identifier }.first()[Stats.value] }
        }
    }
}