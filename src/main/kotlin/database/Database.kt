package xyz.swagbot.database

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.*
import xyz.swagbot.features.guilds.*
import xyz.swagbot.logger
import kotlin.system.exitProcess

lateinit var database: Database

fun <T> sql(sqlcode: Transaction.() -> T): T {
    return transaction(statement = sqlcode)
}

fun Transaction.create(vararg tables: Table) {
    SchemaUtils.create(*tables)
}

fun getDatabaseConnection(login: String, password: String) {
    for (i in 2 downTo 0) {
        try {
            val url = "jdbc:mysql://db:3306/swagbot?useSSL=false&allowPublicKeyRetrieval=true"
            logger.info("Attempting connection to: db:3306")
            database = Database.connect(url, "com.mysql.cj.jdbc.Driver", login, password)

            logger.info("Database connection established.")
            return
        } catch (t: Throwable) {
            t.printStackTrace()
            logger.warn("Could not connect to database... ($i attempts left)")
            Thread.sleep(2000)
        }
    }
    logger.error("Could not connect to the database, exiting...")
    exitProcess(1)
}