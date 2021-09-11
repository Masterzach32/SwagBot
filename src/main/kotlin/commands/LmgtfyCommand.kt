package xyz.swagbot.commands

import com.mojang.brigadier.arguments.StringArgumentType.greedyString
import io.facet.chatcommands.*
import io.facet.common.reply
import io.ktor.http.*

object LmgtfyCommand : ChatCommand(
    name = "Let me google that for you",
    aliases = setOf("lmgtfy", "google")
) {

    override fun DSLCommandNode<ChatCommandSource>.register() {
        argument("query", greedyString()) {
            runs { context ->
                message.reply("https://www.lmgtfy.com/?q=${context.getString("query").encodeURLQueryComponent()}")
            }
        }
    }
}
