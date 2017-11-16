package xyz.swagbot.commands.mod

import net.masterzach32.commands4k.*
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.Permissions
import sx.blah.discord.util.RequestBuffer

/*
 * SwagBot - Created on 9/2/2017
 * Author: Zach Kozar
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at the project root.
 */

/**
 * @author Zach Kozar
 * @version 9/2/2017
 */
object BringCommand: Command("Bring Users", "bring", "here", botPerm = Permission.MOD,
        discordPerms = listOf(Permissions.VOICE_MOVE_MEMBERS)) {

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent,
                         builder: AdvancedMessageBuilder): AdvancedMessageBuilder? {
        if(event.author.getVoiceStateForGuild(event.guild).channel == null)
            return builder.withContent("**You need to be in a voice channel to summon users.**")
        val vc = event.author.getVoiceStateForGuild(event.guild).channel
        event.guild.users
                .filter { it.getVoiceStateForGuild(event.guild).channel != null }
                .forEach { RequestBuffer.request { it.moveToVoiceChannel(vc) } }
        return null
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("", "Brings all users currently connected to a voice channel to you.")
    }
}