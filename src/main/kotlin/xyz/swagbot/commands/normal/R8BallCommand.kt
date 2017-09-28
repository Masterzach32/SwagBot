package xyz.swagbot.commands.normal

import net.masterzach32.commands4k.*
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

object R8BallCommand : Command("8 Ball", "8-ball", "8ball", "8") {

    private val responses = listOf(
            "It is certain", "It is decidedly so", "Without a doubt", "Yes definitely", "You may rely on it",
            "As I see it, yes", "Most likely", "Outlook good", "Yes", "Signs point to yes", "Reply hazy try again",
            "Ask again later", "Better not tell you now", "Cannot predict now", "Concentrate and ask again",
            "Don't count on it", "My reply is no", "My sources say no", "Outlook not so good", "Very doubtful")

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent, permission: Permission): AdvancedMessageBuilder {
        val builder = AdvancedMessageBuilder(event.channel)
        if (args.isEmpty())
            builder.withContent("Ask the 8-ball a question!")
        else
            builder.withContent(responses[(Math.random()*responses.size).toInt()] + ".")
        return builder
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("[question]", "Gives you a prediction to your question.")
    }
}