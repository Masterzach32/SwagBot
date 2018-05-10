package xyz.swagbot.commands.normal

import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Command
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.IVoiceChannel
import sx.blah.discord.util.EmbedBuilder
import xyz.swagbot.commands.getBotLockedMessage
import xyz.swagbot.database.isBotLocked
import xyz.swagbot.dsl.getConnectedVoiceChannel
import xyz.swagbot.utils.RED
import xyz.swagbot.utils.getContent

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
object VoiceCommand : Command("Join/Leave", "summon", "leave", scope = Command.Scope.GUILD) {

    init {
        help.usage["~summon"] = "Summon the bot to the voice channel you are connected to."
        help.usage["~summon <voice channel>"] = "Summon the bot to the specified voice channel."
        help.usage["~leave"] = "Forces the bot to leave a voice channel (if it's connected to one)."
    }

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent,
                         builder: AdvancedMessageBuilder): AdvancedMessageBuilder? {
        if (event.guild.isBotLocked())
            return getBotLockedMessage(builder)

        val embed = EmbedBuilder().withColor(RED)

        val vc: IVoiceChannel?
        if (cmdUsed == aliases[0]) {
            if(args.isNotEmpty()) {
                val vcName = getContent(args, 0)
                vc = event.guild.getVoiceChannelsByName(vcName).firstOrNull()
                if (vc == null) {
                    embed.withDesc("No voice channel with the name **$vcName** exists.")
                    return builder.withEmbed(embed)
                }
            } else {
                vc = event.author.getConnectedVoiceChannel()
                if(vc == null) {
                    embed.withDesc("You need to be in a voice channel to summon the bot.")
                    return builder.withEmbed(embed)
                }
            }
            vc.join()
        } else {
            vc = event.guild.connectedVoiceChannel
            if (vc == null) {
                embed.withDesc("The bot is not currently in a voice channel.")
                return builder.withEmbed(embed)
            }
            vc.leave()
        }
        return null
    }
}