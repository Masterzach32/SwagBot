package xyz.swagbot.commands

import net.masterzach32.commands4k.Command
import net.masterzach32.commands4k.Permission
import net.masterzach32.commands4k.builder.createCommand
import org.jetbrains.exposed.sql.update
import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.util.EmbedBuilder
import xyz.swagbot.database.sb_api_keys
import xyz.swagbot.database.sql
import xyz.swagbot.logger
import xyz.swagbot.utils.BLUE
import xyz.swagbot.utils.ExitCode
import xyz.swagbot.utils.getContent
import xyz.swagbot.utils.shutdown

val ShutdownCommand = createCommand("Shutdown/Restart/Update") {
    aliases("shutdown", "stop", "restart", "update")

    botPerm(Permission.DEVELOPER)

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

    botPerm(Permission.DEVELOPER)

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

val SetMotdCommand = createCommand("Set MOTD") {
    aliases("motd")

    botPerm(Permission.DEVELOPER)

    helpText {
        description = "Set the MOTD."
    }

    onEvent {
        noArgs {
            return@noArgs builder.withContent("**You must specify a message (x to disable).**")
        }

        all {
            sql {
                sb_api_keys.update({sb_api_keys.api_name eq "motd"}) {
                    it[sb_api_keys.api_key] = getContent(args, 0, args.size)
                }
            }

            return@all null
        }
    }
}

val JvmStatsCommand = createCommand("Jvm Stats") {
    aliases("jvmstats")

    botPerm(Permission.DEVELOPER)

    helpText {
        description = "Display memory stats for the JVM."
    }

    onEvent {
        all {
            val embed = EmbedBuilder().withColor(BLUE)

            val runtime = Runtime.getRuntime()

            embed.withTitle("JVM Stats")
            embed.appendField("Used Memory", "${((runtime.totalMemory() - runtime.freeMemory())/Math.pow(2.0, 20.0)).toInt()} MB", true)
            embed.appendField("Max Memory", "${(runtime.totalMemory()/Math.pow(2.0, 20.0)).toInt()} MB", true)

            return@all builder.withEmbed(embed)
        }
    }
}

val ShardStatusCommand = createCommand("Shard Status") {
    aliases("shards")

    botPerm(Permission.DEVELOPER)

    helpText {
        description = "Retrieve current shard status."
    }

    fun getStatusEmoji(check: Boolean): String = if (check) ":white_check_mark:" else ":x:"

    onEvent {
        all {
            val embed = EmbedBuilder().withColor(BLUE)

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