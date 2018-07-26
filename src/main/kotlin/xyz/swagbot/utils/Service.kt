package xyz.swagbot.utils

import com.mashape.unirest.http.Unirest
import net.masterzach32.commands4k.AdvancedMessageBuilder
import org.jetbrains.exposed.sql.deleteAll
import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.util.EmbedBuilder
import xyz.swagbot.audioPlayerManager
import xyz.swagbot.database.TrackStorage
import xyz.swagbot.database.shutdownAudioPlayer
import xyz.swagbot.database.sql
import xyz.swagbot.logger
import xyz.swagbot.status.StatusUpdate
import java.lang.management.ManagementFactory
import java.lang.management.MemoryNotificationInfo
import java.lang.management.MemoryType
import javax.management.NotificationEmitter
import javax.management.NotificationListener

/*
 * SwagBot - Created on 9/1/2017
 * Author: Zach Kozar
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at the project root.
 */

/**
 * Code dealing with the bot process
 *
 * @author Zach Kozar
 * @version 9/1/2017
 */
fun shutdown(client: IDiscordClient): Nothing {
    stop(client, ExitCode.EXITED)
}

fun shutdown(client: IDiscordClient, ec: ExitCode): Nothing {
    stop(client, ec)
}

private fun stop(client: IDiscordClient, ec: ExitCode): Nothing {
    logger.debug("Purging track storage.")
    sql { TrackStorage.deleteAll() }
    logger.info("Shutting down audio player.")
    try {
        client.guilds.forEach { it.shutdownAudioPlayer(true) }
    } catch (t: Throwable) {
        logger.error("Could not shut down audio players gracefully: ${t.message}")
    }
    audioPlayerManager.shutdown()

    StatusUpdate.shutdown()

    Unirest.shutdown()

    logger.info("Attempting to log out of Discord.")
    client.logout()
    logger.info("Attempt successful, exiting.")

    exit(ec)
}

private fun exit(ec: ExitCode): Nothing {
    System.exit(ec.code)
    throw IllegalStateException("System.exit() is not functioning properly!")
}

fun registerMemoryNotifications(client: IDiscordClient) {
    // heuristic to find the tenured pool (largest heap) as seen on http://www.javaspecialists.eu/archive/Issue092.html
    val tenuredGenPool = ManagementFactory.getMemoryPoolMXBeans()
            .first { it.type == MemoryType.HEAP && it.isUsageThresholdSupported }
    // we do something when we reached 85% of memory usage
    tenuredGenPool.collectionUsageThreshold = Math.floor(tenuredGenPool.usage.max * 0.85).toLong()

    // set a listener
    val emitter = ManagementFactory.getMemoryMXBean() as NotificationEmitter
    emitter.addNotificationListener(NotificationListener { n, _ ->
        if (n.type == MemoryNotificationInfo.MEMORY_COLLECTION_THRESHOLD_EXCEEDED) {
            val maxMemory = tenuredGenPool.usage.max.toDouble()
            val usedMemory = tenuredGenPool.usage.used.toDouble()

            AdvancedMessageBuilder(client.applicationOwner.orCreatePMChannel).withEmbed(
                    EmbedBuilder().withColor(RED)
                            .withTitle("Memory usage running high (${(usedMemory/maxMemory*100).toInt()}%), restarting!")
                            .appendField("Max Memory", "${maxMemory / Math.pow(2.0, 20.0)} MB", true)
                            .appendField("Used Memory", "${usedMemory / Math.pow(2.0, 20.0)} MB", true)
            ).build()

            //shutdown(client, ExitCode.OUT_OF_MEMORY)
        }
    }, null, null)
}