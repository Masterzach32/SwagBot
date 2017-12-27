package xyz.swagbot.commands.normal

import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Command
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

object RockPaperScissorsCommand : Command("Rock Paper Scissors", "rock", "paper", "scissors") {

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent,
                         builder: AdvancedMessageBuilder): AdvancedMessageBuilder {
        event.channel.toggleTypingStatus()
        Thread.sleep(1000)
        val rand = when ((Math.random() * 3).toInt()) {
            0 -> "rock"
            1 -> "paper"
            else -> "scissors"
        }
        if (rand == cmdUsed)
            builder.withContent("I choose $rand, its a tie!")
        else if ((rand == "scissors" && cmdUsed == "paper") || (rand == "paper" && cmdUsed == "rock") ||
                (rand == "rock" && cmdUsed == "scissors"))
            builder.withContent("I choose $rand, I win!")
        else
            builder.withContent("I choose $rand, I loose!")
        return builder
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("", "Play rock paper scissors!")
        usage.put("rock", "Choose rock.")
        usage.put("paper", "Choose paper.")
        usage.put("scissors", "Choose scissors.")
    }
}