package xyz.swagbot.commands.normal

import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Command
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.EmbedBuilder
import xyz.swagbot.api.UrbanDefinition
import xyz.swagbot.utils.BLUE
import xyz.swagbot.utils.RED
import xyz.swagbot.utils.getContent

object UrbanDictionaryCommand : Command("Urban Dictionary", "ud") {

    init {
        help.usage["<term>"] = "Look up a term on Urban Dictionary."
    }

    override fun execute(
            cmdUsed: String,
            args: Array<String>,
            event: MessageReceivedEvent,
            builder: AdvancedMessageBuilder
    ): AdvancedMessageBuilder {

        val embed = EmbedBuilder().withColor(BLUE)
        event.channel.toggleTypingStatus()
        val search = getContent(args, 0)
        val def = UrbanDefinition.getDefinition(search)
        if (def != null)
            return builder.withEmbed(embed.withTitle("Urban Dictionary Lookup: ${def.word}").withUrl(def.permalink)
                    .withDesc(def.definition)
                    .appendField("Example:", def.example, true))
        return builder.withEmbed(embed.withColor(RED).withDesc("Couldn't find a definition for **$search**."))
    }
}