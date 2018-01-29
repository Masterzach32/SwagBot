package xyz.swagbot

import com.github.natanbc.discordbotsapi.DiscordBotsAPI
import com.mashape.unirest.http.Unirest
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.typesafe.config.ConfigFactory
import net.masterzach32.commands4k.CommandListener
import net.masterzach32.commands4k.Permission
import org.json.JSONObject
import org.slf4j.LoggerFactory
import sx.blah.discord.api.ClientBuilder
import xyz.swagbot.commands.*
import xyz.swagbot.commands.admin.*
import xyz.swagbot.commands.dev.GarbageCollectionCommand
import xyz.swagbot.commands.dev.ShutdownCommand
import xyz.swagbot.commands.mod.*
import xyz.swagbot.commands.music.*
import xyz.swagbot.commands.normal.*
import xyz.swagbot.database.*
import xyz.swagbot.events.*
import xyz.swagbot.utils.Thread

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

val audioPlayerManager = DefaultAudioPlayerManager()

val cmds = CommandListener({ it?.getCommandPrefix() ?: getDefault("command_prefix") },
        {
            if (it == null)
                this.getBotDMPermission()
            else {
                val perm = this.getBotPermission(it)
                if (it.owner == this && perm != Permission.DEVELOPER)
                    Permission.ADMIN
                else
                    perm
            }
        },
        { _, _ -> })

fun main(args: Array<String>) {
    logger.info("Starting SwagBot version ${config.getString("bot.build")}.")
    getDatabaseConnection("storage/storage.db")

    logger.info("Starting Lavaplayer audio engine.")
    AudioSourceManagers.registerRemoteSources(audioPlayerManager)

    logger.info("Initializing commands.")
    // music
    cmds.add(ClearCommand)
    cmds.add(LeaverClearCommand)
    cmds.add(LoopCommand)
    cmds.add(MoveTrackCommand)
    cmds.add(NowPlayingCommand)
    cmds.add(PauseResumeCommand)
    cmds.add(PlayCommand)
    cmds.add(QueueCommand)
    cmds.add(RemoveDuplicatesCommand)
    cmds.add(RemoveTrackCommand)
    cmds.add(ReplayCommand)
    cmds.add(SearchCommand)
    cmds.add(SeekCommand)
    cmds.add(ShuffleCommand)
    cmds.add(SkipCommand)
    cmds.add(SkipToCommand)
    cmds.add(VoteSkipCommand)
    // normal
    cmds.add(CatCommand)
    cmds.add(DogCommand)
    cmds.add(DonateCommand)
    cmds.add(GameCommand)
    cmds.add(IAmCommand)
    cmds.add(IAmNotCommand)
    cmds.add(InfoCommand)
    cmds.add(JoinCommand)
    cmds.add(InviteCommand)
    cmds.add(LmgtfyCommand)
    cmds.add(MassAfkCommand)
    cmds.add(PingCommand)
    cmds.add(R8BallCommand)
    cmds.add(RockPaperScissorsCommand)
    cmds.add(StrawpollCommand)
    cmds.add(SupportCommand)
    cmds.add(UrbanDictionaryCommand)
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
    //cmds.add(ChatOnlyCommand)
    cmds.add(EditPermissionsCommand)
    // devd
    cmds.add(GarbageCollectionCommand)
    cmds.add(ShutdownCommand)

    cmds.sortCommands()

    logger.info("Creating discord client object.")
    val client = ClientBuilder().withToken(getKey("discord_bot_token")).build()
    logger.info("Registering event listeners.")
    client.dispatcher.registerListener(cmds)
    client.dispatcher.registerListener(GuildCreateHandler)
    client.dispatcher.registerListener(GuildLeaveHandler)
    client.dispatcher.registerListener(ReadyHandler)
    client.dispatcher.registerListener(MessageHandler)
    client.dispatcher.registerListener(NewUserHandler)
    client.dispatcher.registerListener(UserJoinEvent)
    client.dispatcher.registerListener(UserLeaveEvent)
    client.dispatcher.registerListener(UserMovedEvent)
    client.dispatcher.registerListener(RoleHandler)
    client.dispatcher.registerListener(ShardDisconnectHandler)
    client.login()

    logger.info("Waiting to receive guilds...")

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
                messages[4] = "${client.users.size} users"
            }

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
                t.printStackTrace()
            }

            client.changePlayingText(messages[i])
            i++
            if (i == messages.size)
                i = 0
            Thread.sleep((delay*1000).toLong())
        }
    }.start()
}