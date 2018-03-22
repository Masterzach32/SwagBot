package xyz.swagbot.commands.music

import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Command
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.EmbedBuilder
import xyz.swagbot.api.getVideoFromSearch
import xyz.swagbot.api.music.AudioTrackLoadHandler
import xyz.swagbot.audioPlayerManager
import xyz.swagbot.commands.getBotLockedMessage
import xyz.swagbot.commands.getWrongArgumentsMessage
import xyz.swagbot.database.getAudioHandler
import xyz.swagbot.database.isBotLocked
import xyz.swagbot.utils.RED
import xyz.swagbot.utils.getContent

object PlayCommand : Command("Play", "play", "p", scope = Command.Scope.GUILD) {

    init {
        help.usage["<search query>"] = "Searches YouTube for the best matching video and queues it."
        help.usage["<url>"] = "Queues the specified track, playlist or stream in the server's audio player."
    }

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent,
                         builder: AdvancedMessageBuilder): AdvancedMessageBuilder? {
        if(args.isEmpty())
            return getWrongArgumentsMessage(builder, this, cmdUsed)
        if (event.guild.isBotLocked())
            return getBotLockedMessage(builder)
        event.channel.toggleTypingStatus()

        val handler = event.guild.getAudioHandler()

        if (args[0].contains("https://spotify.com") || args[0].contains("https://www.spotify.com") ||
                args[0].contains("https://open.spotify.com"))
            return builder.withEmbed(EmbedBuilder().withColor(RED).withDesc("Sorry, I don't support paid " +
                    "streaming services at the moment."))

        val identifier = if (args[0].contains("http://") || args[0].contains("https://")) args[0]
        else {
            var content = getContent(args, 0)
            if (!content.contains("audio"))
                content += " audio"
            getVideoFromSearch(content)?.getUrl()
        }

        if (identifier == null)
            return builder.withEmbed(EmbedBuilder().withColor(RED).withDesc("Sorry, I could not find a video that" +
                    " matched that description. Try refining your search."))

        audioPlayerManager.loadItemOrdered(handler, identifier, AudioTrackLoadHandler(handler, event, builder))

        if (event.author.getVoiceStateForGuild(event.guild).channel != null &&
                event.client.ourUser.getVoiceStateForGuild(event.guild).channel == null)
            event.author.getVoiceStateForGuild(event.guild).channel.join()

        return null
    }
}