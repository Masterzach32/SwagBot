package xyz.swagbot.commands

import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Command
import net.masterzach32.commands4k.Permission
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.EmbedBuilder
import xyz.swagbot.utils.BLUE

object SupportCommand : Command("Support", "support", botPerm = Permission.NONE) {

    init {
        help.desc = "Links if you need help with SwagBot."
    }

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent,
                         builder: AdvancedMessageBuilder): AdvancedMessageBuilder {
        val embed = EmbedBuilder().withColor(BLUE)
        return builder.withEmbed(embed.withDesc("Need help with SwagBot? Make sure you have read the getting " +
                "started guide: https://swagbot.xyz/gettingstarted\n\n" +
                "Still having trouble? Join the SwagBot support server: https://discord.me/swagbothub\n\n" +
                "If you want to help fix a bug, submit an issue on GitHub: https://github.com/Masterzach32/SwagBot"))
    }
}