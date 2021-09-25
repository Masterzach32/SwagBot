package xyz.swagbot.commands

import discord4j.common.util.Snowflake
import io.facet.commands.GuildApplicationCommand
import io.facet.commands.GuildSlashCommandContext
import io.facet.commands.acknowledge
import io.facet.commands.applicationCommandRequest
import io.facet.common.awaitNullable
import io.facet.common.getConnectedVoiceChannel
import io.facet.common.unwrap
import io.facet.core.feature
import xyz.swagbot.extensions.joinWithAutoDisconnect
import xyz.swagbot.extensions.setTrackContext
import xyz.swagbot.extensions.trackScheduler
import xyz.swagbot.features.music.Music
import java.nio.file.Path

object TTS : GuildApplicationCommand {

    override val guildId = Snowflake.of(97342233241464832)

    override val request = applicationCommandRequest("tts", "Some tts stuff") {
        string("name", "The name of the tts to play.")
    }

    override suspend fun GuildSlashCommandContext.execute() {
        acknowledge(ephemeral = true)

        val guild = getGuild()

        if (member.voiceState.awaitNullable()?.channelId?.unwrap() == null)
            return interactionResponse.sendFollowupMessage("You must be in a voice channel to use this command").let {}

        val name: String by options
        val musicFeature = client.feature(Music)

        val resourceLocation = this::class.java.classLoader.getResource("${name.lowercase()}.mp3")
            ?: return interactionResponse.sendFollowupMessage("Could not find tts: ${name.lowercase()}").let {}

        val item = musicFeature.search(Path.of(resourceLocation.toURI()).toString())
        if (item != null) {
            item.setTrackContext(member, getChannel())
            guild.trackScheduler.queue(item)

            interactionResponse.sendFollowupMessage("Playing.")

            if (guild.getConnectedVoiceChannel() == null)
                member.getConnectedVoiceChannel()?.joinWithAutoDisconnect()
        } else
            interactionResponse.sendFollowupMessage("Could not load audio file.")
    }
}
