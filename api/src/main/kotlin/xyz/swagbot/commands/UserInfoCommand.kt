package xyz.swagbot.commands

import net.masterzach32.commands4k.builder.createCommand
import sx.blah.discord.handle.obj.IUser
import sx.blah.discord.util.EmbedBuilder
import xyz.swagbot.utils.BLUE
import xyz.swagbot.utils.RED
import xyz.swagbot.utils.getContent

/*
 * SwagBot - Created on 7/11/2018
 * Author: Zach Kozar
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at the project root.
 */

/**
 * @author Zach Kozar
 * @version 7/11/2018
 */

val UserInfoCommand = createCommand("User Info") {
    aliases = listOf("userinfo", "user", "ui")

    helpText {
        description = "Get information on a specific user. Can lookup by mention, user id, or name."
    }

    onEvent {
        guild {
            val embed = EmbedBuilder().withColor(BLUE)
            val user: IUser = when {
                event.message.mentions.isNotEmpty() -> event.message.mentions.first()
                args.size == 1 -> event.client.getUserByID(args.first().toLong())
                args.size > 1 -> event.client.getUsersByName(getContent(args, 0), true).firstOrNull()
                else -> null
            } ?: return@guild builder.withEmbed(embed.withColor(RED).withDesc("Could not find that user!"))

            embed.withAuthorName("${user.name}#${user.discriminator}")
            embed.withAuthorIcon(user.avatarURL)

            embed.withDesc("Joined discord on ${user.creationDate.epochSecond}")


            return@guild builder.withEmbed(embed)
        }
    }
}