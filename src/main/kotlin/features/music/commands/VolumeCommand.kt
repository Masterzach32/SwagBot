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
            val channel = getChannel()

            if (!isMusicFeatureEnabled())
                return@runs channel.createEmbed(notPremiumTemplate(prefixUsed)).awaitComplete()

            val volume = client.feature(Music).volumeFor(guildId!!)
            channel.createEmbed(baseTemplate.andThen {
                it.setDescription("Volume is at **$volume**")
            }).await()
        }

        argument("level", integer(0, 100)) {
            runs { context ->
                val channel = getChannel()

                if (!isMusicFeatureEnabled())
                    return@runs channel.createEmbed(notPremiumTemplate(prefixUsed)).awaitComplete()

                val newVolume = context.getInt("level")

//                client.feature(Music).updateVolumeFor(guildId!!, newVolume)
//
//                channel.createEmbed(baseTemplate.andThen {
//                    it.setDescription("Volume changed to **$newVolume**")
//                }).await()

                channel.createEmbed(errorTemplate.andThen {
                    it.setDescription("Volume changing is not supported on alpine-based jvm images.")
                }).awaitComplete()
            }
        }
    }
}
