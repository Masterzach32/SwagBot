package xyz.swagbot.plugins

import sx.blah.discord.api.events.Event

/*
 * SwagBot - Created on 8/4/2018
 * Author: Zach Kozar
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at the project root.
 */

/**
 * @author Zach Kozar
 * @version 8/4/2018
 */
class PluginLoadedEvent(val plugin: Plugin) : Event()