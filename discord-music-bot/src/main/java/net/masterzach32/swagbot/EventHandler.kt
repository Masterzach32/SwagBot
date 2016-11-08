/*
    SwagBot - A Discord Music Bot
    Copyright (C) 2016  Zachary Kozar

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc.,
    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package net.masterzach32.swagbot

import java.io.IOException

import net.masterzach32.swagbot.music.player.YouTubeAudioProvider
import net.masterzach32.swagbot.utils.exceptions.FFMPEGException
import net.masterzach32.swagbot.utils.exceptions.NotStreamableException
import net.masterzach32.swagbot.utils.exceptions.YouTubeDLException
import org.json.JSONObject
import org.slf4j.LoggerFactory

import com.mashape.unirest.http.*
import com.mashape.unirest.http.exceptions.UnirestException

import net.masterzach32.swagbot.api.NSFWFilter
import net.masterzach32.swagbot.commands.Command
import net.masterzach32.swagbot.music.player.AudioTrack
import net.masterzach32.swagbot.utils.Constants
import sx.blah.discord.api.events.EventSubscriber
import sx.blah.discord.handle.impl.events.*
import sx.blah.discord.handle.obj.*
import sx.blah.discord.util.*
import sx.blah.discord.util.audio.events.*

import javax.sound.sampled.UnsupportedAudioFileException

class EventHandler {

    @EventSubscriber
    @Throws(UnsupportedAudioFileException::class, UnirestException::class, FFMPEGException::class, NotStreamableException::class, YouTubeDLException::class, IOException::class, MissingPermissionsException::class)
    fun onGuildCreateEvent(event: GuildCreateEvent) {
        App.guilds.loadGuild(event.guild)
        RequestBuffer.request {
            if (event.client.isReady && event.client.isLoggedIn)
                event.client.changeStatus(Status.game("" + event.client.guilds.size + " servers | ~help"))
        }
    }

    @EventSubscriber
    @Throws(UnsupportedAudioFileException::class, UnirestException::class, FFMPEGException::class, NotStreamableException::class, YouTubeDLException::class, IOException::class, MissingPermissionsException::class)
    fun onGuildLeaveEvent(event: GuildLeaveEvent) {
        App.guilds.removeGuild(event.guild)
    }

    @EventSubscriber
    fun onDiscordDisconnectEvent(event: DisconnectedEvent) {
        logger.error("DISCONNECTED FROM DISCORD")
        logger.error(event.reason.toString())
        App.guilds.saveGuildSettings()
    }

    @EventSubscriber
    @Throws(MissingPermissionsException::class, InterruptedException::class)
    fun onDiscordReconnectedEvent(event: DiscordReconnectedEvent) {
        /*for(IGuild guild : event.getClient().getGuilds()) {
            for(IVoiceChannel channel : guild.getVoiceChannels())
                for(IVoiceChannel connected : event.getClient().getConnectedVoiceChannels())
                    if(connected == channel) {
                        AudioPlayer player = AudioPlayer.getAudioPlayerForGuild(guild);
                        connected.leave();
                        Thread.sleep(500);
                        connected.join();
                    }
        }*/
    }

    @EventSubscriber
    @Throws(MissingPermissionsException::class, RateLimitException::class, DiscordException::class, UnirestException::class, InterruptedException::class)
    fun onReady(event: ReadyEvent) {
        RequestBuffer.request {
            event.client.changeStatus(Status.game("" + event.client.guilds.size + " servers | ~help"))
            App.guilds.applyGuildSettings()
        }
        if (App.prefs.shouldPostBotStats()) {
            val json = Unirest.post("https://bots.discord.pw/api/bots/" + App.prefs.discordClientId + "/stats").header("User-Agent", "SwagBot/1.0 (UltimateDoge)").header("Content-Type", "application/json").header("Authorization", App.prefs.dbAuthKey).body(JSONObject().put("server_count", event.client.guilds.size)).asJson()
            logger.info(json.body.array.getJSONObject(0).toString())
        }
    }

    @EventSubscriber
    @Throws(MissingPermissionsException::class, RateLimitException::class, DiscordException::class, UnirestException::class, IOException::class, UnsupportedAudioFileException::class, YouTubeDLException::class, FFMPEGException::class, NotStreamableException::class)
    fun onMessageEvent(event: MessageReceivedEvent) {
        var message = event.message.content

        if (message.length < 1 || event.message.author != null && event.message.author.isBot)
            return

        if (event.message.channel.isPrivate) {
            try {
                if (message.startsWith(Constants.DEFAULT_COMMAND_PREFIX + "help")) {
                    var params = arrayOfNulls<String>(0)
                    if (message.length > 5)
                        params = message.substring(6).split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    Command.executeCommand(event.message, "help", params)
                } else
                    App.client.getOrCreatePMChannel(event.message.author).sendMessage("**SwagBot** does not currently support DM commands. The only command available to DMs is ``" + Constants.DEFAULT_COMMAND_PREFIX + "help``")
            } catch (e: RateLimitException) {
                e.printStackTrace()
            } catch (e: MissingPermissionsException) {
                e.printStackTrace()
            } catch (e: DiscordException) {
                e.printStackTrace()
            }

            return
        }

        val g = App.guilds.getGuildSettings(event.message.guild)

        if (g.isNSFWFilterEnabled) {
            for (a in event.message.attachments)
                logger.info("attachment: " + a.url + " " + a.filename)
            for (image in event.message.embedded) {
                logger.info("embed: " + image.url)
                if (image.url != null) {
                    val filter = NSFWFilter(image.url)
                    if (filter.isNSFW) {
                        App.client.getOrCreatePMChannel(event.message.author).sendMessage("Your image, `" + filter.url + "` which you posted in **" + event.message.guild.name + "** **" + event.message.channel + "**, was flagged as containing NSFW content, and has been removed. If you believe this is an error, contact the server owner or one of my developers.")
                        event.message.delete()
                    } else if (filter.isPartial) {
                        App.client.getOrCreatePMChannel(event.message.author).sendMessage("Your image, `" + filter.url + "` which you posted in **" + event.message.guild.name + "** **" + event.message.channel + "**, was flagged as containing some or partial NSFW content. Please be aware that NSFW images will be automatically deleted. If you believe this is an error, contact the server owner or one of my developers.")
                    }
                    logger.info("result: nsfw:" + filter.getRaw() + "% partial:" + filter.getPartial() + "% safe:" + filter.getSafe() + "%")
                }
            }
        }

        if (event.message.channel.id == "97342233241464832") {
            if (!event.message.embedded.isEmpty() || !event.message.attachments.isEmpty() || message.contains("http://") || message.contains("https://")) {
                App.waitAndDeleteMessage(App.sendMessage("please don't post links or attachments in " + event.message.channel.mention(), event.message.author, event.message.channel), 30)
                event.message.delete()
                return
            }
        }

        val identifier: String
        val params: Array<String?>
        if (message.indexOf(App.guilds.getGuildSettings(event.message.guild).commandPrefix) == 0) {
            message = message.substring(1, message.length)
            val split = message.split(" ".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
            identifier = split[0]
            params = arrayOfNulls<String>(split.size - 1)
            for (i in params.indices) {
                params[i] = split[i + 1]
            }
            Command.executeCommand(event.message, identifier, params)
        } else if (event.message.mentions.contains(event.client.ourUser) && !event.message.mentionsEveryone()) {
            val split = message.split(" ".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
            val command = arrayOfNulls<String>(split.size - 1)
            for (i in command.indices) {
                command[i] = split[i + 1]
            }
            if(command.size == 0)
                return
            identifier = command[0]!!
            params = arrayOfNulls<String>(command.size - 1)
            for (i in params.indices) {
                params[i] = command[i + 1]
            }
            Command.executeCommand(event.message, identifier, params)
        }
    }

    @EventSubscriber
    @Throws(RateLimitException::class, MissingPermissionsException::class)
    fun onTrackStartEvent(event: TrackStartEvent) {
        try {
            if ((event.player.currentTrack as AudioTrack).shouldAnnounce() && App.guilds.getGuildSettings(event.player.guild).shouldAnnounce())
                App.client.getOrCreatePMChannel((event.player.currentTrack as AudioTrack).user).sendMessage("Your song, **" + (event.player.currentTrack as AudioTrack).title + "** is now playing in **" + event.player.guild.name + "!**")
            if (App.guilds.getGuildSettings(event.player.guild).shouldChangeNick()) {
                var track: String?
                if ((event.player.currentTrack as AudioTrack).title.length > 32)
                    track = (event.player.currentTrack as AudioTrack).title.substring(0, 32)
                else
                    track = (event.player.currentTrack as AudioTrack).title
                if (track == null) {
                    logger.warn("An audio track returned a null value for getTitle() " + event.player.currentTrack.toString())
                    track = "SwagBot"
                }
                try {
                    val str = track
                    RequestBuffer.request {
                        try {
                            event.player.guild.setUserNickname(event.client.ourUser, str)
                        } catch (e: MissingPermissionsException) {
                            e.printStackTrace()
                        } catch (e: DiscordException) {
                            e.printStackTrace()
                        }
                    }
                } catch (e: NullPointerException) {
                    e.printStackTrace()
                }

            }
        } catch (e: DiscordException) {
            logger.warn("Could not send message to " + (event.player.currentTrack as AudioTrack).user.name)
        }

        App.guilds.getGuildSettings(event.player.guild).resetSkipStats()
    }

    @EventSubscriber
    fun onTrackFinishEvent(event: TrackFinishEvent) {
        try {
            if (App.guilds.getGuildSettings(event.player.guild).shouldChangeNick() && event.newTrack.isPresent)
                event.player.guild.setUserNickname(event.client.ourUser, "SwagBot")
            if (event.oldTrack.provider is YouTubeAudioProvider)
                (event.oldTrack.provider as YouTubeAudioProvider).close()
        } catch (e: MissingPermissionsException) {
            e.printStackTrace()
        } catch (e: DiscordException) {
            e.printStackTrace()
        } catch (e: RateLimitException) {
            e.printStackTrace()
        }

    }

    @EventSubscriber
    @Throws(RateLimitException::class, DiscordException::class, MissingPermissionsException::class)
    fun onStatusChangeEvent(event: StatusChangeEvent) {
        App.guilds.forEach { guild -> guild?.dispatchStatusChangedEvent(event) }
    }

    @EventSubscriber
    fun onPauseStateChangeEvent(event: PauseStateChangeEvent) {
    }

    companion object {
        val logger = LoggerFactory.getLogger(EventHandler::class.java)
    }
}