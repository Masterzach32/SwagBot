package xyz.swagbot.commands.normal

import net.masterzach32.commands4k.*
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.EmbedBuilder
import xyz.swagbot.commands.getBotLockedMessage
import xyz.swagbot.commands.getWrongArgumentsMessage
import xyz.swagbot.database.getBotVolume
import xyz.swagbot.database.isBotLocked
import xyz.swagbot.database.setBotVolume
import xyz.swagbot.utils.BLUE
import xyz.swagbot.utils.RED

object VolumeCommand : Command("Change Volume", "volume", "v", scope = Command.Scope.GUILD) {

    init {
        help.usage[""] = "Print the current volume."
        help.usage["<int>"] = "Change the volume of the bot's audio, must be between 0 and 100."
    }

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent,
                         builder: AdvancedMessageBuilder): AdvancedMessageBuilder {
        if (event.guild.isBotLocked())
            return getBotLockedMessage(builder)
        if(args.size > 1)
            return getWrongArgumentsMessage(builder, this, cmdUsed)

        val embed = EmbedBuilder().withColor(BLUE)
        val volume: Int
        if (args.isEmpty())
            return builder.withEmbed(embed.withDesc("Volume is currently set to **${event.guild.getBotVolume()}**"))
        try {
            volume = args[0].toInt()
        } catch (e: NumberFormatException) {
            return builder.withEmbed(embed.withColor(RED).withDesc("Volume must be an integer between 0 - 100"))
        }
        if (volume < 0 || volume > 100)
            return builder.withEmbed(embed.withColor(RED).withDesc("Volume must be set between 0 - 100"))

        event.guild.setBotVolume(volume)
        return builder.withEmbed(embed.withDesc("Volume set to **$volume**"))
    }
}