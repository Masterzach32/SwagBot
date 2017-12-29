package xyz.swagbot

import com.github.natanbc.discordbotsapi.DiscordBotsAPI
import com.mashape.unirest.http.Unirest
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.typesafe.config.ConfigFactory
import net.masterzach32.commands4k.CommandListener
import net.masterzach32.commands4k.Permission
import org.slf4j.LoggerFactory
import sx.blah.discord.api.ClientBuilder
import xyz.swagbot.commands.*
import xyz.swagbot.commands.admin.*
import xyz.swagbot.commands.dev.ShutdownCommand
import xyz.swagbot.commands.mod.*
import xyz.swagbot.commands.music.*
import xyz.swagbot.commands.normal.*
import xyz.swagbot.database.*
import xyz.swagbot.events.*
import xyz.swagbot.utils.Thread
import xyz.swagbot.utils.getTotalUserCount

/*
 * SwagBot - Created on 8/22/17
 * Author: Zach Kozar
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at the project root.
 */

/**
 * @author Zach Kozar
 * @version 8/22/17
 */

val config = ConfigFactory.load()!!
val logger = LoggerFactory.getLogger(config.getString("bot.name"))!!

val audioPlayerManager = DefaultAudioPlayerManager();

val cmds = CommandListener({ it?.getCommandPrefix() ?: getDefault("command_prefix") },
        {
            if (it == null)
                this.getDMPermission()
            else {
                val perm = it.getUserPermission(this)
                if (it.owner == this && perm != Permission.DEVELOPER)
                    Permission.ADMIN
                else
                    perm
            }
        },
        { _, _ -> })

fun main(args: Array<String>) {
    logger.info("Starting ${config.getString("bot.name")} version ${config.getString("bot.build")}.")
    getDatabaseConnection("storage/storage.db")

    AudioSourceManagers.registerRemoteSources(audioPlayerManager)

    // music
    cmds.add(ClearCommand)
    cmds.add(LeaverClearCommand)
    cmds.add(PlayCommand)
    cmds.add(QueueCommand)
    cmds.add(ShuffleCommand)
    cmds.add(SkipCommand)
    cmds.add(SkipToCommand)
    // normal
    cmds.add(CatCommand)
    cmds.add(DogCommand)
    cmds.add(GameCommand)
    cmds.add(JoinCommand)
    cmds.add(InviteCommand)
    cmds.add(LmgtfyCommand)
    cmds.add(MassAfkCommand)
    cmds.add(PingCommand)
    cmds.add(R8BallCommand)
    cmds.add(RockPaperScissorsCommand)
    cmds.add(StrawpollCommand)
    cmds.add(UrlShortenCommand)
    cmds.add(VoiceCommand)
    cmds.add(VolumeCommand)
    // mod
    cmds.add(BringCommand)
    cmds.add(MigrateCommand)
    cmds.add(PruneCommand)
    // admin
    cmds.add(AutoAssignRoleCommand)
    cmds.add(ChangePrefixCommand)
    cmds.add(EditPermissionsCommand)
    // dev
    cmds.add(ShutdownCommand)

    val client = ClientBuilder().withToken(getKey("discord_bot_token")).build()
    client.dispatcher.registerListener(cmds)
    client.dispatcher.registerListener(GuildCreateHandler)
    client.dispatcher.registerListener(ReadyHandler)
    client.dispatcher.registerListener(MessageHandler)
    client.dispatcher.registerListener(NewUserHandler)
    client.dispatcher.registerListener(UserJoinEvent)
    client.dispatcher.registerListener(UserLeaveEvent)
    client.dispatcher.registerListener(UserMovedEvent)
    client.login()

    Thread("Status Message Handler") {
        val api = DiscordBotsAPI(getKey("discord_bots_org"))
        val messages = mutableListOf("", "~h for help", "", "", "", "swagbot.xyz")
        val delay = 240
        while (!client.isReady) {}
        Thread.sleep(30*1000)
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
            if (i == 2)
                messages[2] = "${client.guilds.size} servers"
            if (i == 3) {
                val list = mutableMapOf<String, String>()
                getAllAudioHandlers().forEach { k, v ->
                    if (v.player.playingTrack != null)
                        list.put(k, v.player.playingTrack.info.title)
                }
                val rand = mutableListOf<String>()
                list.forEach { k, _ -> rand.add(k) }
                if (rand.size > 0) {
                    val guild = client.getGuildByID(rand[(Math.random() * rand.size).toInt()].toLong())
                    messages[3] = "${list[guild.stringID]} in ${guild.name}"
                } else
                    i++
            }
            if (i == 4) {
                messages[4] = "${getTotalUserCount(client.guilds)} users"
            }

            val payload = "{ \"server_count\": ${client.guilds.size} }"
            try {
                Unirest.post("https://bots.discord.pw/api/bots/${getKey("discord_client_id")}/stats")
                        .header("Content-Type", "application/json")
                        .header("Authorization", getKey("discord_bots_pw"))
                        .body(payload)
            } catch (t: Throwable) {
                logger.warn("Could not post bot statistics: ${t.message}")
                t.printStackTrace()
            }

            /*try {
                api.postStats(0, 1, client.guilds.size)
            } catch (t: Throwable) {
                logger.warn("Could not post bot statistics: ${t.message}")
                t.printStackTrace()
            }*/

            client.changePlayingText(messages[i])
            i++
            if (i == messages.size)
                i = 0
            Thread.sleep((delay*1000).toLong())
        }
    }.start()
}