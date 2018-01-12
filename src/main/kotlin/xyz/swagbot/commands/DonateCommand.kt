package xyz.swagbot.commands

import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Command
import net.masterzach32.commands4k.Permission
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.EmbedBuilder
import xyz.swagbot.utils.BLUE

object DonateCommand : Command("Donate", "donate", botPerm = Permission.NONE) {

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent,
                         builder: AdvancedMessageBuilder): AdvancedMessageBuilder {
        val embed = EmbedBuilder().withColor(BLUE)
        return builder.withEmbed(embed.withDesc("Help support the development of SwagBot by pledging money on " +
                "Patreon or donating to my PayPal.\n\nhttps://patreon.com/ultimatedoge\n\nhttps://paypal.me/ultimatedoge"))
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("", "Links to help support SwagBot.")
    }
}