package xyz.swagbot.commands.mod

import net.masterzach32.commands4k.*
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.IVoiceChannel
import sx.blah.discord.handle.obj.Permissions
import sx.blah.discord.util.RequestBuffer
import xyz.swagbot.commands.getWrongArgumentsMessage
import xyz.swagbot.dsl.getConnectedVoiceChannel
import xyz.swagbot.dsl.isOnVoice
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

    init {
        help.desc = "Move multiple users between voice channels with a single command."
        help.usage[""] = "Move everyone from the bot's voice channel to your voice channel."
        help.usage["<from> | <to>"] = "Move everyone from one voice channel to another, case-sensitive."
    }

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent,
                         builder: AdvancedMessageBuilder): AdvancedMessageBuilder? {
        val from: IVoiceChannel?
        val to: IVoiceChannel?
        if (args.isEmpty()) {
            if (!event.author.isOnVoice(event.guild))
                return builder.withContent("**Make sure you are in the channel you want to populate!**")
            if (!event.client.ourUser.isOnVoice(event.guild))
                return builder.withContent("**Make sure the bot is the channel that you want to migrate from!**")

            to = event.author.getConnectedVoiceChannel()
            from = event.client.ourUser.getConnectedVoiceChannel(event.guild)
        } else {
            val channels = delimitWithoutEmpty(getContent(args, 0), "\\|")
            if (channels.size != 2)
                return getWrongArgumentsMessage(builder, this, cmdUsed)

            from = event.guild.getVoiceChannelsByName(channels[0]).firstOrNull()
            to = event.guild.getVoiceChannelsByName(channels[1]).firstOrNull()

            if (from == null || to == null)
                return getWrongArgumentsMessage(builder, this, cmdUsed)
        }
        from!!.connectedUsers.forEach { RequestBuffer.request { it.moveToVoiceChannel(to) } }
        return null
    }
}