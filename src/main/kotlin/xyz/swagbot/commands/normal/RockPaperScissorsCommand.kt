package xyz.swagbot.commands.normal

import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Command
import net.masterzach32.commands4k.Permission
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

object RockPaperScissorsCommand : Command("Rock Paper Scissors", "rock", "paper", "scissors") {

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent, permission: Permission): AdvancedMessageBuilder {
        val rand: String
        when ((Math.random() * 3).toInt()) {
            0 -> rand = "rock"
            1 -> rand = "paper"
            else -> rand = "scissors"
        }
        val builder = AdvancedMessageBuilder(event.channel)
        if (rand == cmdUsed)
            builder.withContent("I choose $rand, its a tie!")
        else if ((rand == "scissors" && cmdUsed == "paper") || (rand == "paper" && cmdUsed == "rock") || (rand == "rock" && cmdUsed == "scissors"))
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