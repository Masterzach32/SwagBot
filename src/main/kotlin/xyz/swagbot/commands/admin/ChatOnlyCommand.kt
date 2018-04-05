package xyz.swagbot.commands.admin

import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Command
import net.masterzach32.commands4k.Permission
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.Permissions
import xyz.swagbot.commands.getWrongArgumentsMessage

object ChatOnlyCommand : Command("Set Channel Chat Only", "chatonly", scope = Scope.GUILD,
        botPerm = Permission.ADMIN, discordPerms = listOf(Permissions.MANAGE_CHANNELS)) {

    init {
        help.usage["set <text channel>"] = "Adds a channel to the list of chat-only text channels."
        help.usage["remove <text channel>"] = "Removes the specified channel from the list."
        help.usage["list"] = ""
    }

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent,
                         builder: AdvancedMessageBuilder): AdvancedMessageBuilder {
        if (args.isEmpty())
            return getWrongArgumentsMessage(builder, this, cmdUsed)
        if (args[0] == "list") {

        }
        if (args[0] == "set") {

        }
        return builder
    }
}