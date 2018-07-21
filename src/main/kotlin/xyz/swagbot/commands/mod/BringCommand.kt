package xyz.swagbot.commands.mod

import net.masterzach32.commands4k.*
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.Permissions
import sx.blah.discord.util.RequestBuffer
import xyz.swagbot.dsl.getConnectedVoiceChannel
import xyz.swagbot.dsl.isOnVoice

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
        scope = Command.Scope.GUILD, discordPerms = listOf(Permissions.VOICE_MOVE_MEMBERS)) {

    init {
        help.usage[""] = "Brings all users currently connected to a voice channel to you."
        help.usage["<role mentions>"] = "Brings all users currently connected to a voice channel and in the specified roles to you."
    }

    override fun execute(
            cmdUsed: String,
            args: Array<String>,
            event: MessageReceivedEvent,
            builder: AdvancedMessageBuilder
    ): AdvancedMessageBuilder? {
        if(!event.author.isOnVoice())
            return builder.withContent("**You need to be in a voice channel to summon users.**")
        val vc = event.author.getConnectedVoiceChannel()
        event.guild.users
                .filter { it.isOnVoice(event.guild) }
                .filter {
                    event.message.roleMentions.let { mentions ->
                        if (mentions.isNotEmpty())
                            mentions.any { role -> it.hasRole(role) }
                        else
                            true
                    }
                }
                .forEach { RequestBuffer.request { it.moveToVoiceChannel(vc) } }
        return null
    }
}