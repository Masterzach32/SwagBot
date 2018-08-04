import net.masterzach32.commands4k.Permission
import sx.blah.discord.Discord4J
import sx.blah.discord.util.EmbedBuilder
import xyz.swagbot.DEFAULT_COMMAND_PREFIX
import xyz.swagbot.Stats
import xyz.swagbot.VERSION
import xyz.swagbot.database.commandPrefix
import xyz.swagbot.plugins.createPlugin
import xyz.swagbot.utils.BLUE

createPlugin {
    name = "Basic Bot Commands"
    description = "Get info, invite link, and support commands."
    version = "1.0"

    newCommand("Ping") {
        aliases = listOf("ping")

        botPerm = Permission.NONE

        helpText {
            description = "Pong!"
        }

        onEvent {
            all {
                return@all builder.withContent("Pong!")
            }
        }
    }

    newCommand("Donate") {
        aliases = listOf("donate")

        botPerm = Permission.NONE

        helpText {
            description = "Links to help support SwagBot."
        }

        onEvent {
            all {
                val embed = EmbedBuilder().withColor(BLUE)

                return@all builder.withEmbed(embed.withDesc("Help support the development of SwagBot by pledging " +
                        "money on Patreon or donating to my PayPal.\n\nhttps://patreon.com/ultimatedoge" +
                        "\n\nhttps://paypal.me/ultimatedoge"))
            }
        }
    }

    newCommand("Info") {
        aliases = listOf("info")

        botPerm = Permission.NONE

        helpText {
            description = "Get more info about SwagBot."
        }

        onEvent {
            all {
                val embed = EmbedBuilder().withColor(BLUE)

                embed.withAuthorName("SwagBot v2 ($VERSION)")
                embed.withAuthorIcon("http://swagbot.xyz/images/banner.png")
                embed.withAuthorUrl("http://swagbot.xyz")

                embed.withDesc("SwagBot is a music bot with many additional features. Type **" +
                        (event.guild?.commandPrefix ?: DEFAULT_COMMAND_PREFIX) +
                        "help** to see more commands!\n\n")
                embed.appendDesc("Learn more about SwagBot at https://swagbot.xyz\n\n" +
                        "Follow SwagBot on Twitter for updates:\nhttps://twitter.com/DiscordSwagBot\n" +
                        "Check out the development for SwagBot at:\nhttps://github.com/Masterzach32/SwagBot\n" +
                        "Help development of SwagBot by donating to my PayPal:\nhttps://paypal.me/ultimatedoge\n" +
                        "Or pledge a small amount on Patreon:\nhttps://patreon.com/ultimatedoge\n" +
                        "Join the SwagBot support server:\nhttps://discord.me/swagbothub\n" +
                        "Want to add SwagBot to your server? Click the link below:\n" +
                        "https://discordapp.com/oauth2/authorize?client_id=217065780078968833&scope=bot&permissions=87149640\n")

                embed.withFooterText("\u00a9 SwagBot 2016-2018. Written in Kotlin. Built off of Discord4J " +
                        "${Discord4J.VERSION}.")
                return@all builder.withEmbed(embed)
            }
        }
    }

    newCommand("Invite") {
        aliases = listOf("invite")

        botPerm = Permission.NONE

        helpText {
            description = "Post an invite link for SwagBot."
        }

        onEvent {
            all {
                val embed = EmbedBuilder()
                        .withColor(BLUE)
                        .withTitle("Click this link to add SwagBot to your server!")
                        .withDesc("https://discordapp.com/oauth2/authorize?client_id=217065780078968833&scope=bot&permissions=87149640")
                return@all builder.withEmbed(embed)
            }
        }
    }

    newCommand("Support") {
        aliases = listOf("support")

        botPerm = Permission.NONE

        helpText {
            description = "Links if you need help with SwagBot"
        }

        onEvent {
            val embed = EmbedBuilder().withColor(BLUE)

            all {
                return@all builder.withEmbed(embed.withDesc("Need help with SwagBot? Make sure you have read the " +
                        "getting started guide: https://swagbot.xyz/gettingstarted\n\n" +
                        "Still having trouble? Join the SwagBot support server: https://discord.me/swagbothub\n\n" +
                        "If you want to help fix a bug, submit an issue on GitHub: https://github.com/Masterzach32/SwagBot"))
            }
        }
    }

    newCommand("Bot Statistics") {
        aliases = listOf("stats")

        botPerm = Permission.DEVELOPER

        helpText {
            description = "View stats such as uptime, commands used, etc."
        }

        onEvent {
            all {
                val embed = EmbedBuilder().withColor(BLUE)

                Stats.getStatObjects().forEach { embed.appendField(it.name, "${it.stat}", true) }
                builder.withEmbed(embed)
            }
        }
    }
}