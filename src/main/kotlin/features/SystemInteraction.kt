package xyz.swagbot.features

import discord4j.core.*
import io.facet.discord.*
import reactor.core.publisher.*
import xyz.swagbot.*
import xyz.swagbot.database.*
import xyz.swagbot.extensions.*
import java.time.*
import java.util.concurrent.*

class SystemInteraction {

    private val tasks = mutableListOf<() -> Mono<Void>>()

    val dbTasks: ExecutorService = Executors.newSingleThreadExecutor()

    fun addShutdownTask(task: () -> Mono<Void>) = tasks.add(task)

    companion object : DiscordClientFeature<EmptyConfig, SystemInteraction>("system") {

        override fun install(client: DiscordClient, configuration: EmptyConfig.() -> Unit): SystemInteraction {
            getDatabaseConnection(client, login = System.getenv("DB_USERNAME"), password = System.getenv("DB_PASS"))

            return SystemInteraction().also { feature ->
                Runtime.getRuntime().addShutdownHook(Thread {
                    logger.info("Received shutdown code from system, running shutdown tasks.")
                    feature.tasks.toFlux()
                        .flatMap { it.invoke() }
                        .then(client.logout())
                        .then(feature.dbTasks.shutdownAsync())
                        .timeout(Duration.ofSeconds(10))
                        .block()
                    logger.info("Done.")
                })
            }
        }
    }
}
