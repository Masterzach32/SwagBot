package xyz.swagbot.commands.admin

import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Command
import net.masterzach32.commands4k.Permission
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.Permissions
import sx.blah.discord.util.EmbedBuilder
import xyz.swagbot.commands.getWrongArgumentsMessage
import xyz.swagbot.database.commandPrefix
import xyz.swagbot.utils.BLUE

/*
 * SwagBot - Created on 8/31/2017
 * Author: Zach Kozar
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at the project root.
 */

/**
 * @author Zach Kozar
 * @version 8/31/2017
 */
object ChangePrefixCommand : Command(
        "Change Prefix",
        "changeprefix",
        "cp",
        "prefix",
        scope = Command.Scope.GUILD,
        botPerm = Permission.ADMIN,
        discordPerms = listOf(Permissions.MANAGE_SERVER)
) {

    init {
        help.desc = "Change the command prefix for SwagBot in this server. (can be a single character or a string)"
        help.usage["<string>"] = "The new command prefix for this server."
    }

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent,
                         builder: AdvancedMessageBuilder): AdvancedMessageBuilder {
        if (args.size != 1)
            return getWrongArgumentsMessage(builder, this, cmdUsed)

        val embed = EmbedBuilder().withColor(BLUE)

        event.guild.commandPrefix = args[0]

        embed.withDesc("Command prefix set to **${args[0]}**")

        return builder.withEmbed(embed)
    }
}