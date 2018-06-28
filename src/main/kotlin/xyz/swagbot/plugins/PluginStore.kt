package xyz.swagbot.plugins

import net.masterzach32.commands4k.CommandManager
import xyz.swagbot.logger
import xyz.swagbot.utils.KotlinScriptLoader
import java.io.File

object PluginStore {

    private val loadedPlugins = mutableListOf<Plugin>()

    fun loadAllPlugins(cm: CommandManager) {
        val pluginDir = File("plugins")

        pluginDir.walkTopDown().filter { it.isFile }.forEach {
            logger.info("Attempting to load plugin: ${it.name}")

            val plugin = KotlinScriptLoader.load<Plugin>(it)
            register(plugin, cm)

            logger.info("Loaded plugin: $plugin")
        }
    }

    fun unloadAllPlugins(cm: CommandManager) {
        loadedPlugins.forEach {
            unregister(it, cm)
            logger.info("Unloaded plugin: $it")
        }
    }

    private fun register(plugin: Plugin, cm: CommandManager) {
        plugin.commands.forEach { cm.add(it) }
        plugin.listeners.forEach { cm.dispatcher.registerListener(it) }
        loadedPlugins.add(plugin)
    }

    private fun unregister(plugin: Plugin, cm: CommandManager) {
        plugin.commands.forEach { cm.remove(it) }
        plugin.listeners.forEach { cm.dispatcher.unregisterListener(it) }
        loadedPlugins.remove(plugin)
    }
}