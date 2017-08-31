package xyz.swagbot.commands.mods

import net.masterzach32.commands4k.*
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.IVoiceChannel
import sx.blah.discord.handle.obj.Permissions
import sx.blah.discord.util.RequestBuffer
import xyz.swagbot.utils.Utils

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
object MigrateCommand : Command("Migrate", "migrate", "populate", "m", "move", permission = Permission.MOD) {

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent, permission: Permission): AdvancedMessageBuilder? {
        if (!userHasPermission(event.author, event.guild, Permissions.VOICE_MOVE_MEMBERS))
            return insufficientPermission(event.channel, Permissions.VOICE_MOVE_MEMBERS)
        val from: IVoiceChannel?
        val to: IVoiceChannel?
        if (args.isEmpty()) {
            to = event.author.getVoiceStateForGuild(event.guild).channel
            if (to == null)
                return AdvancedMessageBuilder(event.channel).withContent("**Make sure you are in the channel you want to populate!**")

            from = event.client.ourUser.getVoiceStateForGuild(event.guild).channel
            if (from == null)
                return AdvancedMessageBuilder(event.channel).withContent("**Make sure the bot is the channel that you want to migrate from!**")
        } else {
            val channels = Utils.delimitWithoutEmpty(Utils.getContent(args, 0), "\\|")
            if (channels.size != 2)
                return getWrongArgumentsMessage(event.channel, this, cmdUsed)

            from = event.guild.getVoiceChannelsByName(channels[0])[0]
            to = event.guild.getVoiceChannelsByName(channels[1])[0]

            if (from == null || to == null)
                return getWrongArgumentsMessage(event.channel, this, cmdUsed)
        }
        from.connectedUsers.forEach { RequestBuffer.request { it.moveToVoiceChannel(to) } }
        return null
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("", "Move everyone from the bot's voice channel to your voice channel.")
        usage.put("<from> | <to>", "Move everyone from one voice channel to another, case-sensitive.")
    }
}