package xyz.swagbot.events

import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Permission
import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.EmbedBuilder
import sx.blah.discord.util.MissingPermissionsException
import sx.blah.discord.util.RequestBuffer
import xyz.swagbot.cmds
import xyz.swagbot.database.getCommandPrefix
import xyz.swagbot.database.getUserPermission
import xyz.swagbot.logger
import xyz.swagbot.utils.RED

/*
 * SwagBot - Created on 8/25/17
 * Author: zachk
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at the project root.
 */

/**
 * @author zachk
 * @version 8/25/17
 */
object MessageHandler : IListener<MessageReceivedEvent> {

    override fun handle(event: MessageReceivedEvent) {
        if (event.message.channel.stringID == "97342233241464832") {
            if (!event.message.embeds.isEmpty() || !event.message.attachments.isEmpty() ||
                    event.message.content.contains("http://") || event.message.content.contains("https://")) {

                AdvancedMessageBuilder(event.message.channel)
                        .withContent("${event.message.author} please don't post links or attachments in " +
                                "${event.message.channel}")
                        .withAutoDelete(30)
                        .build()
                event.message.delete()
                return
            }
        }

        if (event.author.isBot ||
                event.channel.isPrivate ||
                !event.message.content.startsWith(event.guild.getCommandPrefix()))
            return

        val identifier: String
        val params: Array<String>

        val tmp = event.message.content.substring(1).split(" ").toTypedArray()
        identifier = tmp[0]
        params = tmp.copyOfRange(1, tmp.size)
        val command = cmds.getCommand(identifier)
        if (command != null) {
            val userPerms = event.guild.getUserPermission(event.author)
            if (userPerms >= command.permission) {
                logger.debug("Shard: ${event.message.shard.info[0]} Guild: ${event.message.guild.stringID} Channel: ${event.channel.stringID} User: ${event.author.stringID} Command: \"${event.message.content}\"")
                val embed = EmbedBuilder().withColor(RED)
                val response = try {
                    command.execute(identifier, params, event, userPerms)
                } catch (e: MissingPermissionsException) {
                    embed.withTitle("Missing Permissions!")
                    embed.withDesc("I need the Discord permission ${e.message} to use that command!")
                    AdvancedMessageBuilder(event.message.channel).withEmbed(embed)
                } catch (t: Throwable) {
                    t.printStackTrace()
                    embed.withTitle("An error occurred while executing that command!")
                    var str = "${t.javaClass.name}: ${t.message}\n"
                    var i = 0
                    while (i < t.stackTrace.size && i < 6) {
                        str += "\tat ${t.stackTrace[i]}\n"
                        i++
                    }
                    if (t.stackTrace.size > 6)
                        str += "\t+ ${t.stackTrace.size-6} more"
                    embed.withDesc(str)
                    AdvancedMessageBuilder(event.message.channel).withEmbed(embed)
                }
                RequestBuffer.request { response?.build() }
            }
        }

    }
}