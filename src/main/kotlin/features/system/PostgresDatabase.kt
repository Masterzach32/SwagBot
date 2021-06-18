package xyz.swagbot.features.system

import discord4j.core.event.*
import discord4j.core.event.domain.lifecycle.*
import io.facet.core.util.*
import io.facet.discord.*
import io.facet.discord.event.*
import io.facet.discord.extensions.*
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.*
import xyz.swagbot.*

class PostgresDatabase private constructor(val database: Database) {

    private val tasks = mutableListOf<suspend () -> Unit>()

    fun addShutdownTask(task: suspend () -> Unit) = tasks.add(task)

    class Config {
        lateinit var databaseName: String
        lateinit var databaseUsername: String
        lateinit var databasePassword: String
    }

    companion object : EventDispatcherFeature<Config, PostgresDatabase>("postgres") {

        override fun EventDispatcher.install(
            scope: CoroutineScope,
            configuration: Config.() -> Unit
        ): PostgresDatabase {
            val config = Config().apply(configuration)

            val database = runBlocking {
                retry(3, 2000) {
                    logger.info("Attempting connection to database.")
                    Database.connect(
                        "jdbc:postgresql://postgres:5432/${config.databaseName}",
                        "org.postgresql.Driver",
                        config.databaseUsername,
                        config.databasePassword
                    )
                }
            }
            logger.info("Connected to postgres database.")

            return PostgresDatabase(database).also { feature ->
                listener<ReadyEvent> { event ->
                    Runtime.getRuntime().addShutdownHook(Thread {
                        logger.info("Received shutdown code from system, running shutdown tasks.")
                        runBlocking {
                            launch {
                                feature.tasks.map { launch { it.invoke() } }.forEach { it.join() }
                                BotScope.cancel()
                                event.client.logout().await()
                            }
                            delay(10_000)
                            cancel("Shutdown tasks took too long, skipping.")
                        }
                        logger.info("Done.")
                    })
                }
            }
        }
    }
}
