package xyz.swagbot.commands.dev

import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Command
import net.masterzach32.commands4k.Permission
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

/*
 * SwagBot - Created on 1/25/2018
 * Author: Zach Kozar
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at the project root.
 */

/**
 * @author Zach Kozar
 * @version 1/25/2018
 */
object GarbageCollectionCommand : Command("Garbage Collection", "gc", botPerm = Permission.DEVELOPER) {

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent,
                         builder: AdvancedMessageBuilder): AdvancedMessageBuilder? {
        event.channel.toggleTypingStatus()
        val memoryUsed = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        System.gc()
        val newMemoryUsed = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        return builder.withContent("Ran garbage collection and freed " +
                "**${((newMemoryUsed - memoryUsed)/Math.pow(2.0, 20.0)).toInt()} MB** of RAM.")
    }
}