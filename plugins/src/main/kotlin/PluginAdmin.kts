import net.masterzach32.commands4k.Permission
import xyz.swagbot.cmds
import xyz.swagbot.plugins.PluginStore
import xyz.swagbot.plugins.createPlugin

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

                return@all builder.withContent("Reloaded plugins.")
            }
        }
    }

    
}