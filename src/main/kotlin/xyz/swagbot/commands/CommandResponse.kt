package xyz.swagbot.commands

import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Command
import sx.blah.discord.util.EmbedBuilder
import xyz.swagbot.utils.BLUE
import xyz.swagbot.utils.RED
import java.awt.Color


fun getWrongArgumentsMessage(builder: AdvancedMessageBuilder, cmd: Command, cmdUsed: String): AdvancedMessageBuilder {
    val embed = EmbedBuilder().withColor(RED)
    embed.withTitle("Oops!")
    embed.withDesc("Incorrect number of arguments. Use `~help $cmdUsed` for details with this command.\n")
    embed.appendDesc("Acceptable parameter types:")
    val map = LinkedHashMap<String, String>()
    cmd.getCommandHelp(map)
    map.forEach { k, v -> embed.appendDesc("\n`$k` $v") }
    return builder.withEmbed(embed).withAutoDelete(30)

}

fun getBotLockedMessage(builder: AdvancedMessageBuilder): AdvancedMessageBuilder {
    return builder.withEmbed(EmbedBuilder().withColor(BLUE).withDesc("**The bot is currently locked.**"))
}

enum class Type { GET, POST }

fun getApiErrorMessage(builder: AdvancedMessageBuilder, type: Type, apiCall: String, body: String, status: Int,
                       statusText: String): AdvancedMessageBuilder {
    val embed = EmbedBuilder().withColor(Color(244, 78, 66))
    embed.withTitle(":warning: The following API call returned a bad status code:")
    embed.withDesc("Error Message: $statusText\n$body")
    embed.appendField("API Call", apiCall, true)
    embed.appendField("Type", type.name, true)
    embed.appendField("Response Code", "$status", true)
    embed.withFooterText("Report this to the dev: https://github.com/Masterzach32/SwagBot")
    embed.withFooterIcon(builder.channel.client.getUserByID("97341976214511616".toLong()).avatarURL)
    return builder.withEmbed(embed)
}