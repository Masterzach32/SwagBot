package xyz.swagbot.plugins

import net.masterzach32.commands4k.CommandManager
import xyz.swagbot.logger
import xyz.swagbot.utils.KotlinScriptLoader
import java.io.File

object PluginStore {

    private const val PLUGIN_DIR = "plugins"

    private val loadedPlugins = mutableListOf<Plugin>()

    fun loadAllPlugins(cm: CommandManager) {
        val pluginFiles = File(PLUGIN_DIR).walkTopDown().filter { it.isFile }.toList()
        logger.info("Found ${pluginFiles.size} plugins.")

        var count = 0
        pluginFiles.forEachIndexed { i, file ->
            logger.info("Attempting to load script ${i+1}/${pluginFiles.size}: ${file.name}")
            try {
                val plugin = KotlinScriptLoader.load<Plugin>(file)
                register(plugin, cm)
                logger.info("Loaded plugin: $plugin")
                count++
            } catch (e: IllegalStateException) {
                logger.info("Could not load script ${file.name}: $e")
            }
        }
        logger.info("Loaded $count plugins")
    }

    fun unloadAllPlugins(cm: CommandManager) {
        loadedPlugins.forEach {
            unregister(it, cm)
            logger.info("Unloaded plugin: $it")
        }
    }

    fun register(plugin: Plugin, cm: CommandManager) {
        plugin.commands.forEach { cm.add(it) }
        plugin.listeners.forEach { cm.dispatcher.registerListener(it) }
        loadedPlugins.add(plugin)
    }

    fun unregister(plugin: Plugin, cm: CommandManager) {
        plugin.commands.forEach { cm.remove(it) }
        plugin.listeners.forEach { cm.dispatcher.unregisterListener(it) }
        plugin.onUnload?.invoke()
        loadedPlugins.remove(plugin)
    }
}