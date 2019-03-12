package admin

import com.vdurmont.emoji.EmojiParser
import net.masterzach32.commands4k.Permission
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.impl.obj.ReactionEmoji
import sx.blah.discord.handle.obj.IEmoji
import sx.blah.discord.util.EmbedBuilder
import xyz.swagbot.cmds
import xyz.swagbot.dsl.request
import xyz.swagbot.dsl.requestGet
import xyz.swagbot.plugins.PluginStore
import xyz.swagbot.plugins.createPlugin
import xyz.swagbot.utils.embedBlue
import xyz.swagbot.utils.embedRed
import xyz.swagbot.utils.getContent
import xyz.swagbot.utils.listOfEmojis

createPlugin {
    name = "Plugin Administration"
    description = "Commands that deal with the plugin system."
    version = "1.0"

    newCommand("Reload Plugins") {
        aliases = listOf("reloadplugins")

        botPerm = Permission.DEVELOPER

        onEvent {
            all {
                PluginStore.unloadAllPlugins(cmds)

                PluginStore.loadAllPlugins(cmds)

                return@all builder.withEmbed(embedBlue("Reloaded all plugins"))
            }
        }
    }

    newCommand("Reload Plugin") {
        aliases = listOf("reloadplugin")

        botPerm = Permission.DEVELOPER

        onEvent {
            all {
                val embed: EmbedBuilder

                val plugin = PluginStore.getPluginByName(getContent(args, 0))

                if (plugin != null) {
                    embed = embedBlue("Reloaded ${plugin.name}")
                } else {
                    embed = embedRed("Could not locate plugin ${getContent(args, 0)}")
                }

                return@all builder.withEmbed(embed)
            }
        }
    }

    newListener<MessageReceivedEvent> {
        if (guild.longID == 97342233241464832 && author.hasRole(guild.getRolesByName("Lux Player").first())) {
            val luxEmojis = listOf(
                    ReactionEmoji.of(EmojiParser.parseToUnicode(":wheelchair:")),
                    ReactionEmoji.of("\uD83C\uDDF1"),
                    ReactionEmoji.of("\uD83C\uDDFA"),
                    ReactionEmoji.of("\uD83C\uDDFD"),
                    ReactionEmoji.of("\uD83C\uDDF5"),
                    ReactionEmoji.of(guild.getEmojiByName("luxplayer")),
                    ReactionEmoji.of("\uD83C\uDDE6"),
                    ReactionEmoji.of("\uD83C\uDDFE"),
                    ReactionEmoji.of("\uD83C\uDDEA"),
                    ReactionEmoji.of("\uD83C\uDDF7")
            )

            luxEmojis.forEach { requestGet { message.addReaction(it) } }
        }
    }
}