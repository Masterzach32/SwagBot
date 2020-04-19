package xyz.swagbot.features

import discord4j.core.*
import io.facet.discord.*
import xyz.swagbot.*
import xyz.swagbot.database.*

class SystemInteraction(config: Config) {

    private val tasks = mutableListOf<Runnable>()

    fun addShutdownTask(task: Runnable) = tasks.add(task)

    fun addShutdownTask(task: () -> Unit) = addShutdownTask(Runnable { task.invoke() })

    class Config

    companion object : DiscordClientFeature<Config, SystemInteraction>("system") {

        override fun install(client: DiscordClient, configuration: Config.() -> Unit): SystemInteraction {
            getDatabaseConnection(login = System.getenv("DB_USERNAME"), password = System.getenv("DB_PASS"))

            return SystemInteraction(Config().apply(configuration)).also { feature ->
                Runtime.getRuntime().addShutdownHook(Thread {
                    logger.info("Received shutdown code from system, running shutdown tasks.")
                    feature.tasks.forEach { it.run() }

                    logger.info("Logging out of Discord.")
                    client.logout().block()

                    logger.info("Done.")
                })
            }
        }
    }
}
