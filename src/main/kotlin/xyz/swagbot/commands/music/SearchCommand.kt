package xyz.swagbot.commands.music

import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Command
import sx.blah.discord.api.events.EventDispatcher
import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IUser
import sx.blah.discord.util.EmbedBuilder
import xyz.swagbot.api.YouTubeVideo
import xyz.swagbot.api.getVideoSetFromSearch
import xyz.swagbot.api.music.AudioTrackLoadHandler
import xyz.swagbot.audioPlayerManager
import xyz.swagbot.commands.getWrongArgumentsMessage
import xyz.swagbot.database.getAudioHandler
import xyz.swagbot.logger
import xyz.swagbot.utils.BLUE
import xyz.swagbot.utils.RED
import xyz.swagbot.utils.getContent

object SearchCommand : Command("Search YouTube", "search") {

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent,
                         builder: AdvancedMessageBuilder): AdvancedMessageBuilder? {
        if (args.isEmpty())
            return getWrongArgumentsMessage(builder, this, cmdUsed)
        event.channel.toggleTypingStatus()
        val embed = EmbedBuilder()

        val list = getVideoSetFromSearch(getContent(args, 0), 5)

        if (list.isEmpty())
            return builder.withEmbed(embed.withColor(RED).withDesc("Sorry, I could not find a video that matched " +
                    "that description. Try refining your search."))

        embed.withColor(BLUE).withTitle("YouTube search result:")
        for (i in 0..(list.size-1)) {
            embed.appendDesc("${i+1}. **${list[i].title}** by **${list[i].channel}**.\n")
        }
        embed.appendDesc("\n${event.author.mention()}, if you would like to queue one of these videos, enter its " +
                "number below within 60 seconds.")

        event.client.dispatcher.registerListener(ResponseListener(event.author, event.channel, list, System.currentTimeMillis()))

        return builder.withEmbed(embed)
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("<search query>", "Searches YouTube for the 5 best matching tracks.")
    }

    class ResponseListener(private val user: IUser, private val channel: IChannel, private val list: List<YouTubeVideo>,
                           private val timestamp: Long) : IListener<MessageReceivedEvent> {

        override fun handle(event: MessageReceivedEvent) {
            if (System.currentTimeMillis()/1000 - timestamp/1000 > 60)
                return unregister(event.client.dispatcher, "Timeout")
            if (event.author == user && event.channel == channel) {
                try {
                    val index = event.message.content.toInt()-1
                    if (index < 0 || index >= list.size)
                        return unregister(event.client.dispatcher, "Bad message")
                    event.channel.toggleTypingStatus()
                    audioPlayerManager.loadItem(list[index].getUrl(), AudioTrackLoadHandler(event.guild.getAudioHandler(),
                            event, AdvancedMessageBuilder(event.channel)))
                    return unregister(event.client.dispatcher, "Successful")
                } catch (t: Throwable) {
                    return unregister(event.client.dispatcher, "Bad message")
                }
            }
        }

        fun unregister(dispatcher: EventDispatcher, reason: String) {
            logger.info("Killing search listener: $reason")
            return dispatcher.unregisterListener(this)
        }
    }
}