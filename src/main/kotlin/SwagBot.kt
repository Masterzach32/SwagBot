import discord4j.core.DiscordClientBuilder
import discord4j.core.event.domain.message.MessageCreateEvent

fun main() {
    val client = DiscordClientBuilder("MjE3MDY1NzgwMDc4OTY4ODMz.XOXzKw.FROLvEtTBra6BLzneZP5azsbEkE").build()

    client.eventDispatcher.on(MessageCreateEvent::class.java)
        .map { it.message }
        .filter { it.content.map { it.startsWith("~droulette") }.orElse(false) }
        .flatMap { msg ->
            msg.guild.flatMap { guild ->
                guild.voiceStates.filter {
                    it.channel.flatMap { it.name }
                }
            }
        }

    client.login().block()
}