package xyz.swagbot.features.system

import discord4j.core.*
import io.facet.core.*
import io.facet.discord.*
import io.facet.discord.extensions.*
import kotlinx.coroutines.*
import reactor.core.publisher.*
import xyz.swagbot.*
import xyz.swagbot.extensions.*
import xyz.swagbot.features.*
import java.time.*
import java.util.concurrent.*

class SystemInteraction private constructor(private val client: DiscordClient) {

    private val tasks = mutableListOf<suspend () -> Unit>()

    val taskScheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)

    fun addShutdownTask(task: suspend () -> Unit) = tasks.add(task)

    companion object : DiscordClientFeature<EmptyConfig, SystemInteraction>("system") {

        override fun install(client: DiscordClient, configuration: EmptyConfig.() -> Unit): SystemInteraction {
            getDatabaseConnection(login = System.getenv("DB_USERNAME"), password = System.getenv("DB_PASS"))

            return SystemInteraction(client).also { feature ->
                Runtime.getRuntime().addShutdownHook(Thread {
                    logger.info("Received shutdown code from system, running shutdown tasks.")
                    runBlocking {
                        GlobalScope.launch {
                            feature.tasks.map { async { it.invoke() } }.forEach { it.await() }

                            val d = async { feature.taskScheduler.shutdown() }

                            client.logout().await()

                            d.await()
                        }
                        delay(10_000)
                    }
                    logger.info("Done.")
                })
            }
        }
    }
}
