package xyz.swagbot.api.game

import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.RED
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.handle.obj.IUser
import sx.blah.discord.util.EmbedBuilder
import sx.blah.discord.util.RequestBuffer
import xyz.swagbot.database.sb_game_brawl
import xyz.swagbot.database.sql
import xyz.swagbot.logger
import java.util.*

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
class Fight(channel: IChannel, users: MutableList<IUser>) : Game("Brawl", channel, users) {

    init {
        start()
    }

    override fun getJoinMessage(user: IUser): String {
        return "${user.getDisplayName(channel.guild)} **joined the brawl!**"
    }

    override fun run() {
        logger.debug("Fight in guild ${channel.guild} starting in 20 seconds")
        sleep(20000)
        if (users.size <= 1) {
            RequestBuffer.request {
                AdvancedMessageBuilder(channel).withEmbed(EmbedBuilder().withColor(RED)
                        .withDesc("The brawl failed to start because not enough users joined!")).build()
            }
            finish()
            return
        }
        inProgress = true
        logger.debug("Fight is starting with ${users.size} users! $users")

        RequestBuffer.request { AdvancedMessageBuilder(channel).withContent("**Let the brawl begin!**").build() }
        sleep(1000)
        val numOfDeathResponses = sql { sb_game_brawl.selectAll().count() }
        while (users.size > 1) {
            var str = ""
            for (j in users.indices)
                if (j < users.size - 1)
                    str += "**${users[j].getDisplayName(channel.guild)}**, "
                else
                    str += "and **${users[j].getDisplayName(channel.guild)}** are fighting!"
            val msg = RequestBuffer.request<IMessage> { AdvancedMessageBuilder(channel).withContent(str).build() }.get()
            sleep(1500)
            var dead: IUser
            do {
                dead = users[Random().nextInt(users.size)]
            } while (dead.longID == 148604482492563456)
            users.remove(dead)
            val killer = users[Random().nextInt(users.size)]
            var result = sql {
                sb_game_brawl.select { sb_game_brawl.id eq Random().nextInt(numOfDeathResponses) }
                        .first().get(sb_game_brawl.death_message)
            }
            result = result.replace("{killed}", "**${dead.getDisplayName(channel.guild)}**")
            result = result.replace("{killer}", "**${killer.getDisplayName(channel.guild)}**")
            val resultString = result
            RequestBuffer.request { msg?.edit(resultString) }
        }
        RequestBuffer.request { AdvancedMessageBuilder(channel).withContent("${users[0]} **won the brawl!**").build() }
        finish()
    }
}