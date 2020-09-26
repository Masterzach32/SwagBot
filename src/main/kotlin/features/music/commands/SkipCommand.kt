package xyz.swagbot.features.music.commands

import io.facet.discord.commands.*
import io.facet.discord.commands.dsl.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*
import xyz.swagbot.extensions.*
import xyz.swagbot.features.permissions.*
import xyz.swagbot.util.*

object SkipCommand : ChatCommand(
    name = "Skip Track",
    aliases = setOf("skip"),
    scope = Scope.GUILD,
    category = "music"
) {

    override fun DSLCommandNode<ChatCommandSource>.register() {
        runs { context ->
            val guild = getGuild()
            val channel = getChannel()

            if (!isMusicFeatureEnabled())
                return@runs channel.createEmbed(notPremiumTemplate(prefixUsed)).awaitComplete()

            if (!hasBotPermission(PermissionType.MOD))
                return@runs channel.createEmbed(errorTemplate.andThen {
                    it.setDescription(
                        "You don't have permission to instantly skip a track. Try using `${prefixUsed}voteskip` instead."
                    )
                }).awaitComplete()

            val voiceChannel = guild.getOurConnectedVoiceChannel()
                ?: return@runs channel.createEmbed(errorTemplate.andThen {
                    it.setDescription("The bot is currently not in a voice channel!")
                }).awaitComplete()

            val memberVs = member!!.voiceState.await()
            if (memberVs.channelId.map { it != voiceChannel.id }.orElse(true))
                return@runs channel.createEmbed(errorTemplate.andThen {
                    it.setDescription("You must be in ${voiceChannel.name} to skip a track!")
                }).awaitComplete()

            val skippedTrack = guild.trackScheduler.playNext()

            if (skippedTrack != null) {
                channel.createEmbed(baseTemplate.andThen {
                    it.setDescription("Skipped track: ${skippedTrack.info.boldFormattedTitle}")
                }).awaitComplete()
            } else {
                channel.createEmbed(errorTemplate.andThen {
                    it.setDescription("Cannot skip as there is no track playing!")
                }).awaitComplete()
            }
        }
    }
}
