package xyz.swagbot.commands

import io.facet.chatcommands.ChatCommand
import io.facet.chatcommands.ChatCommandSource
import io.facet.chatcommands.DSLCommandNode
import io.facet.chatcommands.runs
import io.facet.common.replyEmbed

object Crewlink : ChatCommand(
    name = "CrewLink Info (Among Us)",
    aliases = setOf("crewlink", "amongus"),
    category = "amongus",
    description = "See info for CrewLink proximity chat."
) {

    override fun DSLCommandNode<ChatCommandSource>.register() {
        runs {
            message.replyEmbed {
                title = "CrewLink Proximity Chat (Among Us)"
                thumbnailUrl = "https://github.com/ottomated/CrewLink/raw/master/logo.png"

                description = """
                    This project implements proximity voice chat in Among Us. Everyone in an Among Us lobby with this program running will be able to communicate over voice in-game, with no third-party programs required. Spatial audio ensures that you can only hear people close to you.
                    
                    Download: https://github.com/ottomated/CrewLink/releases
                    CrewLink Server: http://crewlink.masterzach32.net:9736/
                """.trimIndent().trim()
            }
        }
    }
}
