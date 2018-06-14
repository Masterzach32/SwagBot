package xyz.swagbot.events

import net.masterzach32.commands4k.AdvancedMessageBuilder
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.selectAll
import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.guild.GuildCreateEvent
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.Permissions
import sx.blah.discord.util.RequestBuffer
import xyz.swagbot.database.*
import xyz.swagbot.utils.DailyUpdate
import java.time.LocalDateTime
import java.time.ZoneId

/*
 * SwagBot - Created on 8/24/17
 * Author: zachk
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at the project root.
 */

/**
 * @author zachk
 * @version 8/24/17
 */
object GuildCreateHandler : IListener<GuildCreateEvent> {

    private lateinit var guildRows: List<GuildSettingsLoadObj>

    fun pullGuildSettings() {
        guildRows = sql {
            sb_guilds.selectAll().map {
                GuildSettingsLoadObj(
                        it[sb_guilds.id].toLong(),
                        it[sb_guilds.volume],
                        it[sb_guilds.loop],
                        it[sb_guilds.last_voice_channel]?.toLong()
                )
            }
        }
    }

    override fun handle(event: GuildCreateEvent) {
        val now = LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond()
        RequestBuffer.request {
            val joined = event.guild.getJoinTimeForUser(event.client.ourUser).atZone(ZoneId.systemDefault()).toEpochSecond()

            if (now - joined < 30) {
                val channels = event.guild.channels
                        .filter { it.getModifiedPermissions(event.client.ourUser).contains(Permissions.SEND_MESSAGES) }
                val welcomeChannel: IChannel

                welcomeChannel = when {
                    channels.contains(event.guild.defaultChannel) -> event.guild.defaultChannel
                    channels.any {
                        it.name.toLowerCase() == "general" || it.name.toLowerCase() == "chat"
                    } -> channels.first { it.name.toLowerCase() == "general" || it.name.toLowerCase() == "chat"}
                    else -> return@request
                }

                val builder = AdvancedMessageBuilder(welcomeChannel)
                builder.withContent("Thanks for adding me to your server! If you need help, check out the" +
                        " getting started guide on my website: https://swagbot.xyz/gettingstarted")
                RequestBuffer.request { builder.build() }
            }
        }

        event.guild.initialize(guildRows.firstOrNull { it.id == event.guild.longID })
        logger.info("Guild ${event.guild.name} (${event.guild.stringID}) is ready to start receiving commands.")

        DailyUpdate.joinedServer()
    }
}