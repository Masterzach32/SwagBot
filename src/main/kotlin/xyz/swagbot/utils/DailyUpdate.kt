package xyz.swagbot.utils

import net.masterzach32.commands4k.AdvancedMessageBuilder
import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.util.EmbedBuilder
import sx.blah.discord.util.RequestBuffer
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object DailyUpdate : Runnable {

    private val executor = Executors.newScheduledThreadPool(1)
    private var client: IDiscordClient? = null

    private var serversJoined = 0
    private var serversLeft = 0

    fun init(client: IDiscordClient) {
        this.client = client

        executor.scheduleAtFixedRate(this,
                LocalDateTime.now().until(LocalDate.now().plusDays(1).atStartOfDay().minusHours(2),
                        ChronoUnit.MINUTES), 1440, TimeUnit.MINUTES)
    }

    fun shutdown() {
        executor.shutdown()
    }

    override fun run() {
        val client = client!!
        val builder = AdvancedMessageBuilder(client.ourUser.orCreatePMChannel)
        val embed = EmbedBuilder().withColor(BLUE)

        embed.withTitle("Daily Status Update")
        embed.appendField("Servers Joined", "$serversJoined", true)
        embed.appendField("Servers Left", "$serversLeft", true)
        embed.appendField("Servers Count", "${client.guilds.size}", true)

        RequestBuffer.request { builder.withEmbed(embed).build() }
    }

    fun joinedServer() = serversJoined++

    fun leftServer() = serversLeft++
}