package xyz.swagbot.commands.mod

import net.masterzach32.commands4k.*
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.IVoiceChannel
import sx.blah.discord.handle.obj.Permissions
import sx.blah.discord.util.RequestBuffer
import xyz.swagbot.commands.getWrongArgumentsMessage
import xyz.swagbot.utils.delimitWithoutEmpty
import xyz.swagbot.utils.getContent

/*
 * SwagBot - Created on 8/30/2017
 * Author: Zach Kozar
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at the project root.
 */

/**
 * @author Zach Kozar
 * @version 8/30/2017
 */
object MigrateCommand : Command("Migrate", "migrate", "populate", "m", botPerm = Permission.MOD,
        scope = Command.Scope.GUILD, discordPerms = listOf(Permissions.VOICE_MOVE_MEMBERS)) {

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent,
                         builder: AdvancedMessageBuilder): AdvancedMessageBuilder? {
        val from: IVoiceChannel?
        val to: IVoiceChannel?
        if (args.isEmpty()) {
            to = event.author.getVoiceStateForGuild(event.guild).channel
            if (to == null)
                return builder.withContent("**Make sure you are in the channel you want to populate!**")

            from = event.client.ourUser.getVoiceStateForGuild(event.guild).channel
            if (from == null)
                return builder.withContent("**Make sure the bot is the channel that you want to migrate from!**")
        } else {
            val channels = delimitWithoutEmpty(getContent(args, 0), "\\|")
            if (channels.size != 2)
                return getWrongArgumentsMessage(builder, this, cmdUsed)

            from = event.guild.getVoiceChannelsByName(channels[0]).firstOrNull()
            to = event.guild.getVoiceChannelsByName(channels[1]).firstOrNull()

            if (from == null || to == null)
                return getWrongArgumentsMessage(builder, this, cmdUsed)
        }
        from.connectedUsers.forEach { RequestBuffer.request { it.moveToVoiceChannel(to) } }
        return null
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("", "Move everyone from the bot's voice channel to your voice channel.")
        usage.put("<from> | <to>", "Move everyone from one voice channel to another, case-sensitive.")
    }
}