package xyz.swagbot.commands

import net.masterzach32.commands4k.Command
import net.masterzach32.commands4k.Permission
import net.masterzach32.commands4k.builder.createCommand

val PingCommand = createCommand("Ping") {

    aliases("ping")

    scope { Command.Scope.ALL }

    botPerm { Permission.NONE }

    helpText {
        description { "Pong!" }
    }

    onEvent {
        all {
            return@all builder.withContent("Pong!")
        }
    }
}