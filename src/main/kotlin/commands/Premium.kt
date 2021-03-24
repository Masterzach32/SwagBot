package xyz.swagbot.commands

import io.facet.discord.commands.*
import io.facet.discord.commands.dsl.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*
import xyz.swagbot.*
import xyz.swagbot.util.*

object Premium : ChatCommand(
    name = "${EnvVars.BOT_NAME} Premium",
    aliases = setOf("premium"),
    category = "music",
    description = "Learn more about ${EnvVars.BOT_NAME} premium, as well as see premium status in this server."
) {

    private val template = baseTemplate.andThen {
        title = "${EnvVars.BOT_NAME} Premium"

        description = ""

        field {
            name = "Volume Control"
            value = "Enables you to change the music volume for everyone listening."
        }

        field {
            name = "Custom Playlists"
            value = "Import playlists from YouTube, Spotify, and more; They're automatically saved and can be " +
                "queued up on demand."
        }

        field {
            name = "Autoplay"
            value = "${EnvVars.BOT_NAME} can automatically queue similar music to what you've been listening to " +
                "once the queue is empty, so you dont need to worry about adding more songs!"
        }

        field {
            name = "Audio Effects"
            value = "Base boost, adjust EQ, and more."
        }
    }

    override fun DSLCommandNode<ChatCommandSource>.register() {
        runs {
            message.reply(template)
        }
    }
}
