package xyz.swagbot.commands.normal

import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Command
import net.masterzach32.commands4k.Permission
import sx.blah.discord.Discord4J
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.EmbedBuilder
import xyz.swagbot.config
import xyz.swagbot.database.getCommandPrefix
import xyz.swagbot.utils.BLUE

object InfoCommand : Command("Info", "info", botPerm = Permission.NONE) {

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent, builder: AdvancedMessageBuilder): AdvancedMessageBuilder? {
        val embed = EmbedBuilder().withColor(BLUE)

        embed.withAuthorName("SwagBot v2 (${config.getString("bot.build")})")
        embed.withAuthorIcon("http://swagbot.xyz/images/banner.png")
        embed.withAuthorUrl("http://swagbot.xyz")

        embed.withDesc("SwagBot is a music bot with many additional features. Type **${event.guild.getCommandPrefix()}" +
                "help** to see more commands!\n\n")
        embed.appendDesc("Learn more about SwagBot at https://swagbot.xyz\n\n" +
                "Check out the development for SwagBot at:\nhttps://github.com/Masterzach32/SwagBot\n" +
                "Help development of SwagBot by donating to my PayPal:\nhttps://paypal.me/ultimatedoge\n" +
                "Or pledge a small amount on Patreon:\n<https://patreon.com/ultimatedoge>\n" +
                "Join the SwagBot support server:\nhttps://discord.me/swagbothub\n" +
                "Want to add SwagBot to your server? Click the link below:" +
                "\nhttps://discordapp.com/oauth2/authorize?client_id=217065780078968833&scope=bot&permissions=8\n")

        embed.withFooterText("\u00a9 SwagBot 2016-2018. Built off of Discord4J ${Discord4J.VERSION}.")

        return builder.withEmbed(embed)
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("", "Get more info on SwagBot")
    }
}