package xyz.swagbot.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.Table
import org.slf4j.LoggerFactory
import xyz.swagbot.config
import java.sql.Connection
import java.util.*
import java.util.concurrent.Executors

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

val logger = LoggerFactory.getLogger("${config.getString("bot.name")} Database")

val sqlPool = Executors.newFixedThreadPool(1)

val lock = Object()

fun <T> sql(sqlcode: Transaction.() -> T): T {
    synchronized(lock) {
        return transaction(Connection.TRANSACTION_SERIALIZABLE, 1, sqlcode)
    }
}

fun Transaction.create(vararg tables: Table) {
    SchemaUtils.create(*tables)
}

fun getDatabaseConnection(url: String) {
    logger.info("Establishing database connection.")
    for (i in 2 downTo 0) {
        try {
            Database.connect("jdbc:sqlite:$url", "org.sqlite.JDBC")
            return
        } catch (t: Throwable) {
            t.printStackTrace()
            logger.warn("Could not connect to database... ($i attempts left)")
        }
    }
    logger.error("Could not connect to the database, exiting...")
    System.exit(1)
}