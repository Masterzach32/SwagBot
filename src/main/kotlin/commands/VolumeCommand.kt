package xyz.swagbot.commands

import discord4j.rest.util.*
import io.facet.discord.appcommands.*
import io.facet.discord.extensions.*
import xyz.swagbot.extensions.*
import xyz.swagbot.features.music.*

object VolumeCommand : GlobalGuildApplicationCommand {

    override val request = applicationCommandRequest("volume", "Get or update the volume of audio playing from SwagBot.") {
        addOption("level", "The volume level (0-100).", ApplicationCommandOptionType.INTEGER, false)
    }

    override suspend fun GuildInteractionContext.execute() {
        when {
            command.getOption("level").isPresent -> updateVolume(command.getOption("level").get().value.get().asLong().toInt())
            else -> getVolume()
        }
    }

    private suspend fun GuildInteractionContext.getVolume() {
        val guild = getGuild()
        if (!guild.isPremium()) {
            return event.replyEphemeral("Music is a premium feature of SwagBot").await()
        }

        val volume = guild.getVolume()
        event.reply("Volume is at **$volume**").await()
    }

    private suspend fun GuildInteractionContext.updateVolume(newVolume: Int) {
        if (!getGuild().isPremium())
            return event.replyEphemeral("Music is a premium feature of SwagBot").await()

        if (newVolume !in 0..100)
            return event.replyEphemeral("`level` must be between 0 and 100.").await()

        client.feature(Music).updateVolumeFor(guildId, newVolume)

        event.reply("Volume changed to **$newVolume**").await()
    }
}
