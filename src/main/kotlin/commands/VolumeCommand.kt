package xyz.swagbot.commands

import io.facet.commands.GlobalGuildApplicationCommand
import io.facet.commands.GuildSlashCommandContext
import io.facet.commands.applicationCommandRequest
import io.facet.common.await
import xyz.swagbot.extensions.getVolume
import xyz.swagbot.extensions.isPremium

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
        return event.reply("Changing volume is not supported on the arm64v8 architecture.").await()
//
//        val level: Int by options
//        if (!getGuild().isPremium())
//            return event.replyEphemeral("Music is a premium feature of SwagBot").await()
//
//        if (level !in 0..100)
//            return event.replyEphemeral("`level` must be between 0 and 100.").await()
//
//        client.feature(Music).updateVolumeFor(guildId, level)
//
//        event.reply("Volume changed to **$level**").await()
    }
}
