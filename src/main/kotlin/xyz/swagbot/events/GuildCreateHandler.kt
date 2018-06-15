package xyz.swagbot.events

import net.masterzach32.commands4k.AdvancedMessageBuilder
import org.jetbrains.exposed.sql.select
import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.guild.GuildCreateEvent
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IGuild
import sx.blah.discord.handle.obj.Permissions
import sx.blah.discord.util.RequestBuffer
import xyz.swagbot.database.*
import xyz.swagbot.utils.DailyUpdate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import java.util.concurrent.Executors

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

    val initializerExecutor = Executors.newSingleThreadExecutor()!!

    override fun handle(event: GuildCreateEvent) {
        event.guild.initialize()

        if (event.client.isReady)
            initializerExecutor.submit { checkIfNew(event.guild) }

        logger.info("Guild ${event.guild.name} (${event.guild.stringID}) is ready to start receiving commands.")
    }

    private fun checkIfNew(guild: IGuild) {
        val now = LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond()
        val joined = RequestBuffer.request<Long> {
            guild.getJoinTimeForUser(guild.client.ourUser).atZone(ZoneId.systemDefault()).toEpochSecond()
        }.get()

        if (now - joined < 120) {
            val channels = guild.channels
                    .filter {
                        RequestBuffer.request<EnumSet<Permissions>> {
                            it.getModifiedPermissions(guild.client.ourUser)
                        }.get().contains(Permissions.SEND_MESSAGES)
                    }
            val welcomeChannel: IChannel

            welcomeChannel = when {
                channels.contains(guild.defaultChannel) -> guild.defaultChannel
                channels.any {
                    it.name.toLowerCase() == "general" || it.name.toLowerCase() == "chat"
                } -> channels.first { it.name.toLowerCase() == "general" || it.name.toLowerCase() == "chat"}
                else -> return
            }

            val builder = AdvancedMessageBuilder(welcomeChannel)
            builder.withContent("Thanks for adding me to your server! If you need help, check out the" +
                    " getting started guide on my website: https://swagbot.xyz/gettingstarted")
            RequestBuffer.request { builder.build() }
            DailyUpdate.joinedServer()
        }
    }
}