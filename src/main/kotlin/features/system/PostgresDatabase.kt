package xyz.swagbot.features.system

import discord4j.core.event.*
import discord4j.core.event.domain.lifecycle.*
import io.facet.common.*
import io.facet.core.*
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.*
import xyz.swagbot.*
import kotlin.concurrent.*

class PostgresDatabase private constructor(val database: Database) {

    private val tasks = mutableListOf<suspend () -> Unit>()

    fun addShutdownTask(task: suspend () -> Unit) = tasks.add(task)

    class Config {
        lateinit var databaseName: String
        lateinit var databaseUsername: String
        lateinit var databasePassword: String
    }

    companion object : EventDispatcherFeature<Config, PostgresDatabase>("postgres") {

        override suspend fun EventDispatcher.install(
            scope: CoroutineScope,
            configuration: Config.() -> Unit
        ): PostgresDatabase {
            val config = Config().apply(configuration)

            val database = retry(3, 2000) {
                logger.info("Attempting connection to database.")
                withContext(Dispatchers.IO) {
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
                listener<ReadyEvent>(scope) { event ->
                    Runtime.getRuntime().addShutdownHook(thread(start = false) {
                        logger.info("Received shutdown code from system, running shutdown tasks.")
                        try {
                            runBlocking {
                                launch {
                                    feature.tasks.map { launch { it.invoke() } }.forEach { it.join() }
                                    BotScope.cancel()
                                    event.client.logout().await()
                                    this@runBlocking.cancel("Shutdown tasks complete.")
                                }
                                delay(10_000)
                                cancel("Shutdown tasks took too long, skipping.")
                            }
                        } catch (e: CancellationException) {
                            logger.info(e.message)
                        }
                        logger.info("Done.")
                    })
                }
            }
        }
    }
}
