package xyz.swagbot.plugins

import net.masterzach32.commands4k.Command
import net.masterzach32.commands4k.builder.CommandBuilder
import net.masterzach32.commands4k.builder.createCommand
import sx.blah.discord.api.events.Event
import sx.blah.discord.api.events.IListener

class Plugin {

    lateinit var name: String
    lateinit var description: String
    lateinit var version: String
    val commands = mutableListOf<Command>()
    val listeners = mutableListOf<IListener<Event>>()

    internal var onUnload: (() -> Unit)? = null

    fun newCommand(name: String, builder: CommandBuilder.() -> Unit) {
        commands.add(createCommand(name, builder))
    }

    inline fun <reified E : Event> newListener(crossinline event: E.() -> Unit) {
        listeners.add(IListener { (it as? E)?.event() })
    }

    fun onUnload(block: () -> Unit) {
        onUnload = block
    }

    override fun toString(): String = "$name $version"
}

inline fun createPlugin(init: Plugin.() -> Unit) = Plugin().apply(init)