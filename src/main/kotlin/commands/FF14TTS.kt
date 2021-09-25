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

object FF14TTS : GuildApplicationCommand {

    override val guildId = Snowflake.of(97342233241464832)

    override val request = applicationCommandRequest("ff14", "Some tts stuff")

    override suspend fun GuildSlashCommandContext.execute() {
        val guild = getGuild()

        if (member.voiceState.awaitNullable()?.channelId?.unwrap() == null)
            return

        acknowledge(ephemeral = true)

        val musicFeature = client.feature(Music)

        val path = Path.of(this::class.java.classLoader.getResource("ff14.mp3")!!.toURI())
        val item = musicFeature.search(path.toString())

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
