package xyz.swagbot.commands.music

import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Command
import sx.blah.discord.api.events.Event
import sx.blah.discord.api.events.EventDispatcher
import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent
import sx.blah.discord.handle.impl.obj.EmojiImpl
import sx.blah.discord.handle.impl.obj.ReactionEmoji
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IEmoji
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.handle.obj.IUser
import sx.blah.discord.util.EmbedBuilder
import sx.blah.discord.util.RequestBuffer
import xyz.swagbot.api.YouTubeVideo
import xyz.swagbot.api.getVideoSetFromSearch
import xyz.swagbot.api.music.AudioTrackLoadHandler
import xyz.swagbot.audioPlayerManager
import xyz.swagbot.commands.getWrongArgumentsMessage
import xyz.swagbot.database.getAudioHandler
import xyz.swagbot.dsl.getConnectedVoiceChannel
import xyz.swagbot.dsl.isOnVoice
import xyz.swagbot.logger
import xyz.swagbot.utils.BLUE
import xyz.swagbot.utils.RED
import xyz.swagbot.utils.getContent

object SearchCommand : Command("Search YouTube", "search", "ytsearch", scope = Scope.GUILD) {

    init {
        help.usage["<search query>"] = "Searches YouTube for the 5 best matching videos."
    }

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent,
                         builder: AdvancedMessageBuilder): AdvancedMessageBuilder? {
        if (args.isEmpty())
            return getWrongArgumentsMessage(builder, this, cmdUsed)
        event.channel.toggleTypingStatus()
        val embed = EmbedBuilder()

        val list = getVideoSetFromSearch(getContent(args, 0), 5)

        if (list.isEmpty())
            return builder.withEmbed(embed.withColor(RED).withDesc("Sorry, I could not find a video that " +
                    "matched that description. Try refining your search."))

        embed.withColor(BLUE).withTitle("YouTube Search Result")
        for (i in 0 until list.size) {
            embed.appendDesc("${i+1}. [**${list[i].title}** by **${list[i].channel}**](${list[i].getUrl()}).\n")
        }
        embed.appendDesc("\n${event.author}, if you would like to queue one of these videos, enter its " +
                "number below within 60 seconds.")

        val message = RequestBuffer.request<IMessage> { builder.withEmbed(embed).build() }.get()

        event.client.dispatcher.registerListener(ReactionResponseListener(message, event.author, event.channel, list,
                System.currentTimeMillis()))

        return null
    }

    private class MessageResponseListener(
            user: IUser,
            channel: IChannel,
            list: List<YouTubeVideo>,
            timestamp: Long
    ) : ResponseListener<MessageReceivedEvent>(user, channel, list, timestamp) {

        override fun handle(event: MessageReceivedEvent) {
            if (System.currentTimeMillis()/1000 - timestamp/1000 > 60)
                return unregister(event.client.dispatcher, "Timeout")
            if (event.author == user && event.channel == channel) {
                try {
                    val index = event.message.content.toInt()-1
                    if (index < 0 || index >= list.size)
                        return unregister(event.client.dispatcher, "Bad message")
                    event.channel.toggleTypingStatus()
                    audioPlayerManager.loadItemOrdered(channel.guild.getAudioHandler(),
                            list[index].getUrl(), AudioTrackLoadHandler(event.guild.getAudioHandler(),
                            event, AdvancedMessageBuilder(event.channel)))

                    if (event.client.ourUser.getVoiceStateForGuild(event.guild).channel == null &&
                            event.author.isOnVoice())
                        event.author.getConnectedVoiceChannel()!!.join()

                    return unregister(event.client.dispatcher, "Successful")
                } catch (t: Throwable) {
                    return unregister(event.client.dispatcher, "Bad message")
                }
            }
        }
    }

    private class ReactionResponseListener(
            val message: IMessage,
            user: IUser,
            channel: IChannel,
            list: List<YouTubeVideo>,
            timestamp: Long
    ) : ResponseListener<ReactionAddEvent>(user, channel, list, timestamp) {

        init {
            list.mapIndexed { i, _ -> ReactionEmoji.of(":${emojiUnicode[i]}:") }
                    .forEach { RequestBuffer.request { message.addReaction(it) } }
        }

        override fun handle(event: ReactionAddEvent) {
            if (event.message == message && event.author == user && event.channel == channel) {
                logger.info(event.reaction.emoji.name)
            }
        }

        companion object {
            val emojiUnicode = listOf("one", "two", "three", "four", "five")
        }
    }

    private abstract class ResponseListener<E : Event>(
            val user: IUser,
            val channel: IChannel,
            val list: List<YouTubeVideo>,
            val timestamp: Long
    ) : IListener<E> {

        protected fun unregister(dispatcher: EventDispatcher, reason: String) {
            logger.debug("Killing search listener: $reason")
            return dispatcher.unregisterListener(this)
        }
    }
}