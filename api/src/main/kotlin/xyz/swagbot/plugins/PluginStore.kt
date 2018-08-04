package xyz.swagbot.plugins

import net.masterzach32.commands4k.CommandManager
import xyz.swagbot.logger
import xyz.swagbot.utils.KotlinScriptLoader
import java.io.File

object PluginStore {

    private const val PLUGIN_DIR = "plugins"

    private val loadedPlugins = mutableListOf<Plugin>()

    init {
        System.setProperty("idea.io.use.fallback", "true")
    }

    fun loadAllPlugins(cm: CommandManager) {
        val pluginFiles = File(PLUGIN_DIR).walkTopDown().filter { it.isFile }.toList()
        logger.info("Found ${pluginFiles.size} plugins.")

        var count = 0
        pluginFiles.forEachIndexed { i, file ->
            logger.info("Attempting to load script ${i+1}/${pluginFiles.size}: ${file.name}")
            val plugin = loadPlugin(file)
            if (plugin != null) {
                logger.info("Loaded plugin: $plugin")
                register(plugin, cm)
                count++
            } else {
                logger.info("Could not load script ${file.name}. Check for compile errors.")
            }
        }
        logger.info("Loaded $count plugins.")
    }

    fun loadPlugin(file: File): Plugin? {
        try {
            return KotlinScriptLoader.load<Plugin>(file)
        } catch (e: IllegalStateException) {
            return null
        }
    }

    fun unloadAllPlugins(cm: CommandManager) {
        loadedPlugins.forEach {
            unregister(it, cm)
            logger.info("Unloaded plugin: $it")
        }
    }

    fun getPluginByName(name: String): Plugin? = loadedPlugins.firstOrNull { it.name.contains(name) }

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