package xyz.swagbot.features.system

import discord4j.core.*
import io.facet.discord.*
import io.facet.discord.extensions.*
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.*
import xyz.swagbot.*
import xyz.swagbot.util.*

class PostgresDatabase private constructor(
    private val client: GatewayDiscordClient,
    val database: Database
) {

    private val tasks = mutableListOf<suspend () -> Unit>()

    fun addShutdownTask(task: suspend () -> Unit) = tasks.add(task)

    class Config {
        lateinit var databaseName: String
        lateinit var databaseUsername: String
        lateinit var databasePassword: String
    }

    companion object : DiscordClientFeature<Config, PostgresDatabase>("postgres") {

        override fun install(client: GatewayDiscordClient, configuration: Config.() -> Unit): PostgresDatabase {
            val config = Config().apply(configuration)

            val database = runBlocking {
                retry(3, 2000) {
                    logger.info("Attempting connection to: postgres:5432")
                    Database.connect(
                        "jdbc:postgresql://postgres:5432/${config.databaseName}",
                        "org.postgresql.Driver",
                        config.databaseUsername,
                        config.databasePassword
                    )
                }
            }
            logger.info("Connected to postgres database.")

            return PostgresDatabase(client, database).also { feature ->
                Runtime.getRuntime().addShutdownHook(Thread {
                    logger.info("Received shutdown code from system, running shutdown tasks.")
                    runBlocking {
                        GlobalScope.launch {
                            feature.tasks.map { async { it.invoke() } }.forEach { it.await() }
                            client.scope.cancel()
                            client.logout().await()
                        }
                        delay(10_000)
                    }
                    logger.info("Done.")
                })
            }
        }
    }
}
