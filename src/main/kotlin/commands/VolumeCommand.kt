package xyz.swagbot.commands

import io.facet.commands.GlobalGuildApplicationCommand
import io.facet.commands.GuildSlashCommandContext
import io.facet.commands.applicationCommandRequest
import io.facet.common.await
import xyz.swagbot.extensions.getVolume
import xyz.swagbot.extensions.isPremium
import xyz.swagbot.extensions.setVolume

object VolumeCommand : GlobalGuildApplicationCommand {

    override val request =
        applicationCommandRequest("volume", "Get or update the volume of audio playing from SwagBot.") {
            int("level", "The volume level (0-100).", false)
        }

    override suspend fun GuildSlashCommandContext.execute() {
        when {
            "level" in options -> updateVolume()
            else -> getVolume()
        }
    }

    private suspend fun GuildSlashCommandContext.getVolume() {
        val guild = getGuild()
        if (!guild.isPremium()) {
            return event.reply("Music is a premium feature of SwagBot").withEphemeral(true).await()
        }

        val volume = guild.getVolume()
        val icon = when {
            volume == 0 -> ":mute:"
            volume <= 60 -> ":sound:"
            else -> ":loud_sound:"
        }
        event.reply("$icon Volume: **$volume**").await()
    }

    private suspend fun GuildSlashCommandContext.updateVolume() {
        val guild = getGuild()
        val level: Int by options
        if (!guild.isPremium())
            return event.reply("Music is a premium feature of SwagBot")
                .withEphemeral(true)
                .await()

        if (level !in 0..100)
            return event.reply("`level` must be between 0 and 100.")
                .withEphemeral(true)
                .await()

        guild.setVolume(level)

        event.reply("Volume changed to **$level**").await()
    }
}
