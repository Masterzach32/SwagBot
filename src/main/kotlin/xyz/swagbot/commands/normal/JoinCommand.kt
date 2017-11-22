package xyz.swagbot.commands.normal

import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Command
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.EmbedBuilder
import xyz.swagbot.api.game.GameManager
import xyz.swagbot.database.getCommandPrefix
import xyz.swagbot.utils.BLUE
import xyz.swagbot.utils.RED

/*
 * SwagBot - Created on 11/17/2017
 * Author: Zach Kozar
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at the project root.
 */

/**
 * @author Zach Kozar
 * @version 11/17/2017
 */
object JoinCommand : Command("Join Game", "join", scope = Command.Scope.GUILD) {

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent,
                         builder: AdvancedMessageBuilder): AdvancedMessageBuilder {
        val embed = EmbedBuilder()

        if (!GameManager.isGameRunning(event.channel))
            return builder.withEmbed(embed.withColor(RED).withDesc("There is no game in progress! Start one by typing `${event.guild.getCommandPrefix()}fight`!").build()) as AdvancedMessageBuilder
        if (GameManager.getGame(event.channel).isInProgress())
            return builder.withEmbed(embed.withColor(RED).withDesc("Game is already in progress! Wait for this one to finish and then start a new one!").build()) as AdvancedMessageBuilder
        if (GameManager.getGame(event.channel).addUser(event.author))
            return builder.withEmbed(embed.withColor(BLUE).withDesc(GameManager.getGame(event.channel).getJoinMessage(event.author)).build()) as AdvancedMessageBuilder
        return builder.withEmbed(embed.withColor(RED).withDesc("You have already joined the game! It should be starting shortly.").build()) as AdvancedMessageBuilder
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("", "Join the current game, if there is one.")
    }

}