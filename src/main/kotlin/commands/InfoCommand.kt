package xyz.swagbot.commands

import com.mojang.brigadier.*
import com.mojang.brigadier.builder.*
import discord4j.common.*
import discord4j.core.*
import io.facet.discord.commands.*
import io.facet.discord.commands.extensions.*
import xyz.swagbot.util.*

object InfoCommand : ChatCommand(
    name = "Info",
    aliases = setOf("info"),
    scope = Scope.GUILD
) {

    override fun register(client: DiscordClient, node: LiteralArgumentBuilder<ChatCommandSource>) {
        node.executesAsync { context ->
            context.source.message.channel.flatMap { channel ->
                channel.createEmbed(baseTemplate.andThen {
                    it.setTitle("SwagBot v3 (3.0.0)")
                    it.setDescription("""
SwagBot is a music bot with many additional features. Type `~help` to see more commands!

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
https://discordapp.com/oauth2/authorize?client_id=217065780078968833&scope=bot&permissions=87149640
""".trimIndent()
                    )

                    it.setFooter(
                        "\u00a9 SwagBot 2016-2020. Written in Kotlin. Built off of Discord4J" +
                                " ${GitProperties.getProperties()[GitProperties.APPLICATION_VERSION]}",
                        null
                    )
                })
            }.then()
        }
    }
}
