package xyz.swagbot.features.music.commands

import io.facet.discord.commands.*
import io.facet.discord.commands.dsl.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*
import kotlinx.coroutines.flow.*
import xyz.swagbot.extensions.*

object LeaverClear : ChatCommand(
    name = "Leaver Cleanup",
    aliases = setOf("leavercleanup"),
    scope = Scope.GUILD,
    category = "music",
    description = "Remove tracks from the queue that were added by members no longer in voice."
) {

    override fun DSLCommandNode<ChatCommandSource>.register() {
        runs {
            val guild = getGuild()
            val voiceChannel = guild.getOurConnectedVoiceChannel() ?: return@runs

            val removed = guild.trackScheduler.pruneQueue(voiceChannel.getConnectedMemberIds().toSet())

            message.reply("Removed **${removed.size}** tracks from the queue.")
        }
    }
}
