package xyz.swagbot.commands

import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Command
import net.masterzach32.commands4k.Permission
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.EmbedBuilder
import xyz.swagbot.utils.BLUE

object InviteCommand : Command("Invite SwagBot", "invite", botPerm = Permission.NONE) {

    init {
        help.desc = "Post an invite link for SwagBot."
    }

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent,
                         builder: AdvancedMessageBuilder): AdvancedMessageBuilder {

        val embed = EmbedBuilder()
                .withColor(BLUE)
                .withTitle("Click this link to add SwagBot to your server!")
                .withDesc("https://discordapp.com/oauth2/authorize?client_id=217065780078968833&scope=bot&permissions=8")
        return builder.withEmbed(embed)
    }
}