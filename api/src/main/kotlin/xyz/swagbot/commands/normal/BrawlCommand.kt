package xyz.swagbot.commands.normal

import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Command
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.IUser
import sx.blah.discord.handle.obj.StatusType
import sx.blah.discord.util.EmbedBuilder
import xyz.swagbot.api.game.Fight
import xyz.swagbot.api.game.GameManager
import xyz.swagbot.database.commandPrefix
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
object BrawlCommand : Command("Games", "fight", "brawl", scope = Command.Scope.GUILD) {

    init {
        help.desc = "Play a game with your fellow server members!"
    }

    override fun execute(
            cmdUsed: String,
            args: Array<String>,
            event: MessageReceivedEvent,
            builder: AdvancedMessageBuilder
    ): AdvancedMessageBuilder? {

        val users = mutableSetOf<IUser>()
        val embed = EmbedBuilder()
        if (event.message.mentionsEveryone())
            event.message.guild.users
                    .forEach { users.add(it) }
        else if (event.message.mentionsHere())
            event.message.guild.users
                    .filter { it.presence.status == StatusType.ONLINE }
                    .forEach { users.add(it) }
        else if (event.message.roleMentions.isNotEmpty())
            event.message.roleMentions
                    .forEach { event.message.guild.getUsersByRole(it)
                            .forEach { users.add(it) } }
        else
            event.message.mentions
                    .forEach { users.add(it) }

        if (users.isEmpty())
            users.add(event.author)

        if (!GameManager.isGameInProgress(event.channel)) {
            GameManager.addGame(Fight(event.channel, users.toMutableList()))
            return builder.withEmbed(embed.withColor(BLUE).withDesc("A $cmdUsed will be starting in 20 seconds! " +
                    "Type `${event.guild.commandPrefix}join` to join!"))
        } else
            return builder.withEmbed(embed.withColor(RED).withDesc("A game is already in progress!"))
    }
}