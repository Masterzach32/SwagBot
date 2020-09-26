package xyz.swagbot.commands

import com.mojang.brigadier.arguments.StringArgumentType.*
import io.facet.discord.commands.*
import io.facet.discord.commands.dsl.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*
import java.net.*

object LmgtfyCommand : ChatCommand(
    name = "Let me google that for you",
    aliases = setOf("lmgtfy", "google")
) {

    override fun DSLCommandNode<ChatCommandSource>.register() {
        argument("query", greedyString()) {
            runs { context ->
                getChannel()
                    .createMessage("https://www.lmgtfy.com/?q=${URLEncoder.encode(context.getString("query"), "UTF-8")}")
                    .awaitComplete()
            }
        }
    }
}
