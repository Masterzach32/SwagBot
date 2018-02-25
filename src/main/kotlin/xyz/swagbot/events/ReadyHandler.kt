package xyz.swagbot.events

import com.github.natanbc.discordbotsapi.DiscordBotsAPI
import com.mashape.unirest.http.Unirest
import org.json.JSONObject
import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.ReadyEvent
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

        Thread("Status Message Handler") {
            val api = DiscordBotsAPI(getKey("discord_bots_org"))
            val messages = mutableListOf("", "~h for help", "", "", "", "swagbot.xyz")
            val delay = 240

            logger.info("Starting status message thread.")

            var i = 0
            while (true) {
                if (i == 0) {
                    val motd = getKey("motd")
                    if (motd == "x")
                        i++
                    else
                        messages[0] = motd
                }
                if (i == 2) {
                    messages[2] = "${event.client.guilds.size} servers"

                    val payload = JSONObject()
                    payload.put("server_count", event.client.guilds.size)
                    try {
                        Unirest.post("https://bots.discord.pw/api/bots/${getKey("discord_client_id")}/stats")
                                .header("Content-Type", "application/json")
                                .header("Authorization", getKey("discord_bots_pw"))
                                .body(payload)
                    } catch (t: Throwable) {
                        logger.warn("Could not post bot statistics: ${t.message}")
                        t.printStackTrace()
                    }

                    try {
                        api.postStats(0, 1, event.client.guilds.size)
                    } catch (t: Throwable) {
                        logger.warn("Could not post bot statistics: ${t.message}")
                        t.printStackTrace()
                    }
                }
                if (i == 3) {
                    val list = mutableMapOf<String, String>()
                    getAllAudioHandlers().forEach { k, v ->
                        if (v.player.playingTrack != null)
                            list.put(k, v.player.playingTrack.info.title)
                    }
                    val rand = mutableListOf<String>()
                    list.forEach { k, _ -> rand.add(k) }
                    if (rand.size > 0) {
                        val guild = event.client.getGuildByID(rand[(Math.random() * rand.size).toInt()].toLong())
                        messages[3] = "${list[guild.stringID]} in ${guild.name}"
                    } else
                        i++
                }
                if (i == 4) {
                    messages[4] = "${event.client.users.size} users"
                }

                event.client.changePlayingText(messages[i])
                i++
                if (i == messages.size)
                    i = 0
                Thread.sleep((delay*1000).toLong())
            }
        }.start()
    }
}