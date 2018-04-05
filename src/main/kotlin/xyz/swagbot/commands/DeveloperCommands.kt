package xyz.swagbot.commands

import net.masterzach32.commands4k.Command
import net.masterzach32.commands4k.Permission
import net.masterzach32.commands4k.builder.createCommand
import sx.blah.discord.api.IDiscordClient
import xyz.swagbot.logger
import xyz.swagbot.utils.ExitCode
import xyz.swagbot.utils.shutdown

val ShutdownCommand = createCommand("Shutdown/Restart/Update") {
    aliases("shutdown", "stop", "restart", "update")

    hidden { true }
    scope { Command.Scope.ALL }
    botPerm { Permission.DEVELOPER }

    onEvent {
        all {
            when (cmdUsed) {
                "shutdown", "stop" -> {
                    logger.info("Shutting down.")
                    shutdown(event.client)
                }
                "restart" -> {
                    logger.info("Restarting...")
                    shutdown(event.client, ExitCode.RESTART_REQUESTED)
                }
                "update" -> {
                    logger.info("Requesting update and restarting...")
                    shutdown(event.client, ExitCode.UPDATE_REQUESTED)
                }
                else -> {
                    builder.withContent("Unknown command.")
                }
            }
            return@all null
        }
    }
}

val GarbageCollectionCommand = createCommand("Run Garbage Collection") {
    aliases("gc")

    hidden { true }
    scope { Command.Scope.ALL }
    botPerm { Permission.DEVELOPER }

    helpText {
        description { "Run the Java garbage collector." }
    }

    onEvent {
        all {
            event.channel.toggleTypingStatus()
            val memoryUsed = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
            System.gc()
            val newMemoryUsed = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
            return@all builder.withContent("Ran garbage collection and freed " +
                    "**${((memoryUsed - newMemoryUsed)/Math.pow(2.0, 20.0)).toInt()} MB** of Heap space.")
        }
    }
}