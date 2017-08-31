package xyz.swagbot.commands

import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Command
import net.masterzach32.commands4k.Permission
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.EmbedBuilder
import xyz.swagbot.cmds
import xyz.swagbot.database.getDefault
import xyz.swagbot.utils.BLUE
import xyz.swagbot.utils.RED
import java.util.*

/*
 * SwagBot - Created on 8/26/2017
 * Author: Zach Kozar
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at the project root.
 */

/**
 * @author Zach Kozar
 * @version 8/26/2017
 */

object HelpCommand : Command("Help", "help", "h") {

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent, permission: Permission): AdvancedMessageBuilder {
        val builder: AdvancedMessageBuilder
        val embed = EmbedBuilder().withColor(BLUE)
        if (args.isEmpty()) {
            if (!event.channel.isPrivate) {
                embed.withDesc("${event.author.mention()} A list of commands has been sent to your direct messages!")
                AdvancedMessageBuilder(event.channel).withEmbed(embed).build()
            }
            val defaultCommandPrefix = getDefault("command_prefix")
            builder = AdvancedMessageBuilder(event.client.getOrCreatePMChannel(event.author))
            embed.withTitle("Help and Info:")
            embed.withDesc("")
            println(permission)
            var i = 0
            while (i <= permission.ordinal) {
                var str = ""
                cmds.getCommandList()
                        .filter { it.permission == Permission.values()[i] }
                        .forEach { str += defaultCommandPrefix + it.aliases[0] + "\n" }
                if (str.isEmpty())
                    str = "There are no commands for this permission level."
                embed.appendField(Permission.values()[i].name, str, true)
                i++
            }
            embed.withDesc(
                    "**Note**: Command prefixes may be different per guild!\n" +
                            "**Permissions**: ${Permission.values().toList()}\n" +
                            "To view more information for a command, use `${defaultCommandPrefix}help <command>`\n\n" +
                            "Check out the development for SwagBot at:\nhttps://github.com/Masterzach32/SwagBot\n" +
                            "Help development of SwagBot by donating to my PayPal:\nhttps://paypal.me/ultimatedoge\n" +
                            "Or pledge a small amount on Patreon:\n<https://patreon.com/ultimatedoge>\n" +
                            "Join SwagBot Hub:\nhttps://discord.me/swagbothub\n" +
                            "Want to add SwagBot to your server? Click the link below:\nhttps://discordapp.com/oauth2/authorize?client_id=217065780078968833&scope=bot&permissions=8\n\n" +
                            "Note you can only see the commands available to you with your permission **$permission** in **${event.guild.name}**")
            builder.withEmbed(embed.build())
        } else {
            builder = AdvancedMessageBuilder(event.channel).withEmbed(embed.withColor(RED).withDesc("No command found with alias `${args[0]}`").build()) as AdvancedMessageBuilder
            cmds.getCommandList()
                    .filter { it.aliases.contains(args[0]) }
                    .forEach {
                        embed.withColor(BLUE)
                        embed.withTitle("Command: **${it.name}**")
                        embed.appendField("Aliases:", "${it.aliases}", true)
                        embed.appendField("Permission Required:", "${it.permission}", true)
                        var str = ""
                        val map = HashMap<String, String>()
                        it.getCommandHelp(map)
                        map.forEach { k, v -> str += "\n`${if (k.isEmpty()) "default" else k}` $v" }
                        if (str.isEmpty())
                            str = "No help text."
                        embed.withDesc(str)
                        builder.withEmbed(embed.build())
                    }

        }
        return builder
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("", "Display a list of commands.")
        usage.put("<command>", "Display detailed information about that command.")
    }
}