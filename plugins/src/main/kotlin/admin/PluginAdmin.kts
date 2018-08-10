package admin

import net.masterzach32.commands4k.Permission
import sx.blah.discord.util.EmbedBuilder
import xyz.swagbot.cmds
import xyz.swagbot.plugins.PluginStore
import xyz.swagbot.plugins.createPlugin
import xyz.swagbot.utils.embedBlue
import xyz.swagbot.utils.embedRed
import xyz.swagbot.utils.getContent

createPlugin {
    name = "Plugin Administration"
    description = "Commands that deal with the plugin system."
    version = "1.0"

    newCommand("Reload Plugins") {
        aliases = listOf("reloadplugins")

        botPerm = Permission.DEVELOPER
        hidden = true

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
        hidden = true

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
}