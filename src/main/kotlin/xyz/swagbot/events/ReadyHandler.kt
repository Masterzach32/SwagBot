package xyz.swagbot.events

import com.github.natanbc.discordbotsapi.DiscordBotsAPI
import com.mashape.unirest.http.Unirest
import org.json.JSONObject
import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.ReadyEvent
import sx.blah.discord.handle.obj.ActivityType
import sx.blah.discord.handle.obj.StatusType
import sx.blah.discord.util.RequestBuffer
import xyz.swagbot.database.getAllAudioHandlers
import xyz.swagbot.database.getKey
import xyz.swagbot.database.getLastVoiceChannel
import xyz.swagbot.logger
import xyz.swagbot.utils.Thread

/*
 * SwagBot - Created on 8/24/17
 * Author: zachk
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at the project root.
 */

/**
 * @author zachk
 * @version 8/24/17
 */
object ReadyHandler : IListener<ReadyEvent> {

    override fun handle(event: ReadyEvent) {
        // join all voice channels the bot was in before it was shut down
        event.client.guilds.forEach { RequestBuffer.request { it.getLastVoiceChannel()?.join() } }
        logger.info("Startup complete.")
    }
}