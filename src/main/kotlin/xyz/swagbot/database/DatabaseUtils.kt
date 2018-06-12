package xyz.swagbot.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.Table
import org.slf4j.LoggerFactory
import xyz.swagbot.utils.ExitCode
import java.sql.Connection

/*
 * SwagBot - Created on 8/22/17
 * Author: Zach Kozar
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at the project root.
 */

/**
 * @author Zach Kozar
 * @version 8/22/17
 */

val logger = LoggerFactory.getLogger("SwagBot Database")!!

lateinit var db: Database
lateinit var type: DatabaseType

val lock = Object()

fun <T> sql(sqlcode: Transaction.() -> T): T {
    return when (type) {
        DatabaseType.MYSQL -> transaction(statement = sqlcode)
        DatabaseType.SQLITE -> { synchronized(lock) { transaction(Connection.TRANSACTION_SERIALIZABLE, 1, db, sqlcode) } }
    }
}

fun Transaction.create(vararg tables: Table) {
    SchemaUtils.create(*tables)
}

fun getDatabaseConnection(args: Array<String>): Database {
    if (args.isEmpty()) {
        logger.info("You need to pass the database location / driver as arguments!")
        System.exit(ExitCode.LOGIN_FAILURE.code)
    }
    type = DatabaseType.valueOf(args[0])
    val loc = args[1]
    val login = if (args.size > 2) Pair(args[2], args[3]) else null
    for (i in 2 downTo 0) {
        try {
            logger.info("Attempting connection to: \"${type.getUrl(loc)}\" using \"${type.driver}\"")
            val database = if (login != null)
                Database.connect(type.getUrl(loc), type.driver, login.first, login.second)
            else
                Database.connect(type.getUrl(loc), type.driver)

            db = database

            // make sure tables are initialized
            sql {
                create(sb_api_keys, sb_defaults, sb_guilds, sb_permissions, sb_chat_channels, sb_stats, sb_game_brawl,
                        sb_iam_roles, sb_track_storage, sb_music_profile, sb_game_switcher)
            }

            return database
        } catch (t: Throwable) {
            logger.warn("Could not connect to database: ${t::class.simpleName}: ${t.message} ($i attempts left)")
            Thread.sleep(1000)
        }
    }
    throw Exception("Could not connect to the database, exiting...")
}

enum class DatabaseType(private val url: String, val driver: String) {
    SQLITE("jdbc:sqlite:", "org.sqlite.JDBC"),
    MYSQL("jdbc:mysql:", "com.mysql.cj.jdbc.Driver");

    fun getUrl(loc: String): String = url + loc
}