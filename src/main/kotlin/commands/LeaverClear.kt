package xyz.swagbot.commands

import io.facet.chatcommands.*
import io.facet.common.*
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
            val voiceChannel = guild.getConnectedVoiceChannel() ?: return@runs

            val removed = guild.trackScheduler.pruneQueue(voiceChannel.getConnectedMemberIds().toSet())

            message.reply("Removed **${removed.size}** tracks from the queue.")
        }
    }
}
