package xyz.swagbot.commands

import com.mojang.brigadier.arguments.StringArgumentType.*
import io.facet.discord.commands.*
import io.facet.discord.commands.dsl.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*
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