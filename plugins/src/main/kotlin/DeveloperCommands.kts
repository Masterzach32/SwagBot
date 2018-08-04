import net.masterzach32.commands4k.Permission
import org.jetbrains.exposed.sql.update
import xyz.swagbot.database.ApiKeys
import xyz.swagbot.database.sql
import xyz.swagbot.logger
import xyz.swagbot.plugins.createPlugin
import xyz.swagbot.utils.*
import kotlin.math.pow

createPlugin {
    name = "Developer Commands"
    description = ""
    version = "1.0"

    newCommand("Shutdown/Restart/Update") {
        aliases = listOf("shutdown", "stop", "restart", "update")

        botPerm = Permission.DEVELOPER

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

    newCommand("Run Garbage Collection") {
        aliases = listOf("gc")

        botPerm = Permission.DEVELOPER

        helpText {
            description = "Run the Java garbage collector."
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

    newCommand("Set MOTD") {
        aliases = listOf("motd")

        botPerm = Permission.DEVELOPER

        helpText {
            description = "Set the MOTD."
        }

        onEvent {
            noArgs {
                return@noArgs builder.withContent("**You must specify a message (x to disable).**")
            }

            all {
                sql {
                    ApiKeys.update({ApiKeys.api_name eq "motd"}) {
                        it[ApiKeys.api_key] = getContent(args, 0, args.size)
                    }
                }

                return@all null
            }
        }
    }

    newCommand("Jvm Stats") {
        aliases = listOf("jvmstats")

        botPerm = Permission.DEVELOPER

        helpText {
            description = "Display memory stats for the JVM."
        }

        onEvent {
            all {
                val embed = embedBlue()

                val runtime = Runtime.getRuntime()

                embed.withTitle("JVM Stats")
                embed.appendField("Used Memory", "${((runtime.totalMemory() - runtime.freeMemory())/2.0.pow(2)).toInt()} MB", true)
                embed.appendField("Max Memory", "${(runtime.totalMemory()/2.0.pow(2)).toInt()} MB", true)

                return@all builder.withEmbed(embed)
            }
        }
    }

    newCommand("Shard Status") {
        aliases = listOf("shards")

        botPerm = Permission.DEVELOPER

        helpText {
            description = "Retrieve current shard status."
        }

        fun getStatusEmoji(check: Boolean): String = if (check) ":white_check_mark:" else ":x:"

        onEvent {
            all {
                val embed = embedBlue()

                embed.withTitle("Shard Count: ${event.client.shards.size}")
                embed.withDesc("First box indicates login status, second box indicates if shard is ready.")

                event.client.shards.forEach {
                    embed.appendField(
                            "Shard ${it.info[0]}: ${getStatusEmoji(it.isLoggedIn)}${getStatusEmoji(it.isReady)}",
                            "Servers: ${it.guilds.size}\nUsers: ${it.users.size}\nPing: ${it.responseTime}",
                            true
                    )
                }

                return@all builder.withEmbed(embed)
            }
        }
    }
}
