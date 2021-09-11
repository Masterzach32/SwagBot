package xyz.swagbot.commands

import io.facet.chatcommands.*
import io.facet.common.await
import io.facet.common.getConnectedVoiceChannel
import io.facet.common.reply
import xyz.swagbot.extensions.hasBotPermission
import xyz.swagbot.extensions.isMusicFeatureEnabled
import xyz.swagbot.extensions.trackScheduler
import xyz.swagbot.features.music.notPremiumTemplate
import xyz.swagbot.features.music.trackSkippedTemplate
import xyz.swagbot.features.permissions.PermissionType

object SkipCommand : ChatCommand(
    name = "Skip Track",
    aliases = setOf("skip"),
    scope = Scope.GUILD,
    category = "music"
) {

    override fun DSLCommandNode<ChatCommandSource>.register() {
        runs { context ->
            val guild = getGuild()

            if (!isMusicFeatureEnabled()) {
                message.reply(notPremiumTemplate(prefixUsed))
                return@runs
            }

            if (!hasBotPermission(PermissionType.MOD)) {
                message.reply(
                    "You don't have permission to instantly skip a track. Use `${prefixUsed}voteskip` instead."
                )
                return@runs
            }

            val voiceChannel = guild.getConnectedVoiceChannel() ?: return@runs

            val memberVs = member.voiceState.await()
            if (memberVs.channelId.map { it != voiceChannel.id }.orElse(true)) {
                message.reply("You must be in ${voiceChannel.name} to skip a track!")
                return@runs
            }

            val skippedTrack = guild.trackScheduler.playNext()

            if (skippedTrack != null) {
                message.reply(
                    trackSkippedTemplate(
                        member.displayName,
                        skippedTrack,
                        guild.trackScheduler.player.playingTrack
                    )
                )
            } else {
                message.reply("Cannot skip as there is no track playing!")
            }
        }
    }
}
