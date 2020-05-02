package xyz.swagbot.database

import discord4j.core.*
import io.facet.discord.extensions.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.*
import reactor.core.publisher.*
import xyz.swagbot.features.*
import xyz.swagbot.logger
import java.util.concurrent.*
import kotlin.system.exitProcess

lateinit var database: Database

private lateinit var dc: DiscordClient

val systemExec by lazy { dc.feature(SystemInteraction).dbTasks }

fun <T> sql(sqlcode: Transaction.() -> T): Mono<T> = Mono.create {
    systemExec.submit {
        try {
            it.success(transaction(database, sqlcode))
        } catch (e: Throwable) {
            it.error(e)
        }
    }
}

fun Transaction.create(vararg tables: Table) {
    SchemaUtils.create(*tables)
}

fun getDatabaseConnection(client: DiscordClient, login: String, password: String) {
    dc = client
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