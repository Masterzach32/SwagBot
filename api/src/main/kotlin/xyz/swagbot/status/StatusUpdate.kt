package xyz.swagbot.status

import com.github.natanbc.discordbotsapi.DiscordBotsAPI
import com.mashape.unirest.http.Unirest
import org.json.JSONObject
import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.handle.obj.ActivityType
import sx.blah.discord.handle.obj.StatusType
import sx.blah.discord.util.RequestBuffer
import xyz.swagbot.database.getAllAudioHandlers
import xyz.swagbot.database.getKey
import xyz.swagbot.logger
import xyz.swagbot.utils.addShutdownHook
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object StatusUpdate : Runnable {

    private const val delay = 240L

    private var client: IDiscordClient? = null
    private val messages = mutableListOf<StatusMessage>()
    private val api = DiscordBotsAPI(getKey("discord_bots_org"))
    private val executor = Executors.newScheduledThreadPool(1)

    fun init(client: IDiscordClient) {
        this.client = client
        // message of the day
        messages.add(StatusMessage {
            val motd = getKey("motd")
            if (motd == "x")
                return@StatusMessage null
            else
                return@StatusMessage motd
        })

        // !help
        messages.add(StatusMessage {
            return@StatusMessage "~h for help"
        })

        // server count
        messages.add(StatusMessage {
            val payload = JSONObject()
            payload.put("server_count", client.guilds.size)
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
                api.postStats(0, 1, client.guilds.size)
            } catch (t: Throwable) {
                logger.warn("Could not post bot statistics: ${t.message}")
            }

            return@StatusMessage "${client.guilds.size} servers"
        })

        // random song
        messages.add(StatusMessage {
            val list = mutableMapOf<Long, String>()
            getAllAudioHandlers().forEach { k, v ->
                if (v.player.playingTrack != null)
                    list[k] = v.player.playingTrack.info.title
            }
            val rand = mutableListOf<Long>()
            list.forEach { k, _ -> rand.add(k) }
            if (rand.size > 0) {
                val guild = client.getGuildByID(rand[(Math.random() * rand.size).toInt()])
                return@StatusMessage "${list[guild.longID]} in ${guild.name}"
            } else
                return@StatusMessage null
        })

        // user count
        messages.add(StatusMessage {
            return@StatusMessage "${client.users.size} users"
        })

        // website
        messages.add(StatusMessage {
            return@StatusMessage "swagbot.xyz"
        })

        executor.scheduleAtFixedRate(this, 0, delay, TimeUnit.SECONDS)

       addShutdownHook { executor.shutdown() }
    }

    override fun run() {
        val nextStatus = messages.removeAt(0)
        val message = nextStatus.message
        messages.add(nextStatus)
        if (message != null)
            RequestBuffer.request { client!!.changePresence(StatusType.ONLINE, ActivityType.PLAYING, message) }
        else
            run()
    }
}