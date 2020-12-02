package xyz.swagbot.features.music.commands

import com.mojang.brigadier.arguments.IntegerArgumentType.*
import io.facet.discord.commands.*
import io.facet.discord.commands.dsl.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*
import xyz.swagbot.extensions.*
import xyz.swagbot.features.music.*
import xyz.swagbot.util.*

object VolumeCommand : ChatCommand(
    name = "Volume",
    aliases = setOf("volume", "v"),
    scope = Scope.GUILD,
    category = "music"
) {

    override fun DSLCommandNode<ChatCommandSource>.register() {
        runs {

            if (!isMusicFeatureEnabled()) {
                message.reply(notPremiumTemplate(prefixUsed))
                return@runs
            }

            val volume = client.feature(Music).getVolumeFor(guildId!!)
            message.reply("Volume is at **$volume**")
        }

        argument("level", integer(0, 100)) {
            runs { context ->
                if (!isMusicFeatureEnabled()) {
                    message.reply(notPremiumTemplate(prefixUsed))
                    return@runs
                }

                val newVolume = context.getInt("level")

                client.feature(Music).updateVolumeFor(guildId!!, newVolume)

                message.reply("Volume changed to **$newVolume**")

                //message.reply("Volume changing is not supported on alpine-based jvm images.")
            }
        }
    }
}
