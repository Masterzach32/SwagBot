package xyz.swagbot.features.music.commands

import io.facet.discord.commands.*
import io.facet.discord.commands.dsl.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*
import xyz.swagbot.extensions.*
import xyz.swagbot.features.music.*
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

            if (!isMusicFeatureEnabled()) {
                respondEmbed(notPremiumTemplate(prefixUsed))
                return@runs
            }

            if (!hasBotPermission(PermissionType.MOD)) {
                respondEmbed(errorTemplate.andThen {
                    description = "You don't have permission to instantly skip a track. " +
                            "Try using `${prefixUsed}voteskip` instead."
                })
                return@runs
            }

            val voiceChannel = guild.getOurConnectedVoiceChannel()
            if (voiceChannel == null) {
                respondEmbed(errorTemplate.andThen {
                    description = "The bot is currently not in a voice channel!"
                })
                return@runs
            }

            val memberVs = member.voiceState.await()
            if (memberVs.channelId.map { it != voiceChannel.id }.orElse(true)) {
                respondEmbed(errorTemplate.andThen {
                    description = "You must be in ${voiceChannel.name} to skip a track!"
                })
                return@runs
            }

            val skippedTrack = guild.trackScheduler.playNext()

            if (skippedTrack != null) {
                respondEmbed(baseTemplate.andThen {
                    description = "Skipped track: ${skippedTrack.info.boldFormattedTitle}"
                })
            } else {
                respondEmbed(errorTemplate.andThen {
                    description = "Cannot skip as there is no track playing!"
                })
            }
        }
    }
}
