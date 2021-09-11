package xyz.swagbot.commands

import discord4j.common.GitProperties
import io.facet.chatcommands.ChatCommand
import io.facet.chatcommands.ChatCommandSource
import io.facet.chatcommands.DSLCommandNode
import io.facet.chatcommands.runs
import io.facet.common.dsl.and
import io.facet.common.reply
import xyz.swagbot.EnvVars
import xyz.swagbot.util.baseTemplate

object InfoCommand : ChatCommand(
    name = "Info",
    aliases = setOf("info")
) {

    override fun DSLCommandNode<ChatCommandSource>.register() {
        runs {
            message.reply(baseTemplate.and {
                title =
                    "SwagBot v3 ${if (EnvVars.CODE_ENV == "test") "Development Version" else ""} (${EnvVars.CODE_VERSION})"
                description = """
SwagBot is a music bot with many additional features. Type `${EnvVars.DEFAULT_COMMAND_PREFIX}help` to see more commands!

Learn more about SwagBot at https://swagbot.xyz

Follow SwagBot on Twitter for updates:
https://twitter.com/DiscordSwagBot
Check out the development for SwagBot at:
https://github.com/Masterzach32/SwagBot
Help development of SwagBot by donating to my PayPal:
https://paypal.me/ultimatedoge
Or pledge a small amount on Patreon:
https://patreon.com/ultimatedoge
Join the SwagBot support server:
https://discord.me/swagbothub
Want to add SwagBot to your server? Click the link below:
https://discordapp.com/oauth2/authorize?client_id=${client.selfId.asLong()}&scope=bot&permissions=87149640
""".trimIndent()

                footer(
                    "\u00a9 SwagBot 2016-2020. Written in Kotlin. Built off of Discord4J " +
                        "${GitProperties.getProperties()[GitProperties.APPLICATION_VERSION]}",
                    null
                )
            })
        }
    }
}
