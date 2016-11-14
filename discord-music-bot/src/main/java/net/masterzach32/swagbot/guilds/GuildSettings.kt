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
package net.masterzach32.swagbot.guilds

import java.io.BufferedWriter
import java.io.FileWriter
import java.io.IOException
import java.util.ArrayList

import com.google.gson.GsonBuilder
import com.mashape.unirest.http.exceptions.UnirestException
import net.masterzach32.commands4j.editMessage
import net.masterzach32.commands4j.waitAndDeleteMessage
import net.masterzach32.swagbot.App
import net.masterzach32.swagbot.music.PlaylistManager
import net.masterzach32.swagbot.music.player.*
import net.masterzach32.swagbot.utils.GUILD_JSON
import net.masterzach32.swagbot.utils.GUILD_SETTINGS
import net.masterzach32.swagbot.utils.exceptions.FFMPEGException
import net.masterzach32.swagbot.utils.exceptions.NotStreamableException
import net.masterzach32.swagbot.utils.exceptions.YouTubeAPIException
import net.masterzach32.swagbot.utils.exceptions.YouTubeDLException
import sx.blah.discord.handle.impl.events.StatusChangeEvent
import sx.blah.discord.handle.obj.IGuild
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.handle.obj.IUser
import sx.blah.discord.util.DiscordException
import sx.blah.discord.util.MissingPermissionsException
import sx.blah.discord.util.RequestBuffer
import sx.blah.discord.util.audio.AudioPlayer

import javax.sound.sampled.UnsupportedAudioFileException

data class GuildSettings(@Transient val iGuild: IGuild, var commandPrefix: Char, var maxSkips: Int, var volume: Int, var botLocked: Boolean, var nsfwFilter: Boolean, var announce: Boolean, var changeNick: Boolean, var lastChannel: String?, var queue: MutableList<String>?, val statusListener: StatusListener) {

    @Transient val playlistManager: PlaylistManager
    @Transient private val skipIDs: MutableList<String>

    init {
        playlistManager = PlaylistManager(iGuild.id)
        skipIDs = ArrayList<String>()
    }

    fun resetSkipStats() {
        skipIDs.clear()
    }

    fun addSkipID(user: IUser) {
        skipIDs.add(user.id)
    }

    fun hasUserSkipped(userID: String): Boolean {
        return skipIDs.contains(userID)
    }

    fun numUntilSkip(): Int {
        return maxSkips - skipIDs.size
    }

    fun dispatchStatusChangedEvent(event: StatusChangeEvent): Boolean {
        return statusListener.passEvent(event)
    }

    fun saveSettings(): GuildSettings {
        playlistManager.save()

        val tracks = audioPlayer.playlist
        queue = ArrayList<String>()
        for (track in tracks)
            if (track != null)
                queue!!.add((track as AudioTrack).url)

        try {
            val fout = BufferedWriter(FileWriter("${GUILD_SETTINGS}${iGuild.id}/$GUILD_JSON"))
            fout.write(GsonBuilder().setPrettyPrinting().create().toJson(this))
            fout.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return this
    }

    fun applySettings(): GuildSettings {
        RequestBuffer.request {
            try {
                if (App.client.ourUser.getDisplayName(iGuild) != "SwagBot")
                    iGuild.setUserNickname(App.client.ourUser, "SwagBot")
            } catch (e: MissingPermissionsException) {
                e.printStackTrace()
            } catch (e: DiscordException) {
                e.printStackTrace()
            }
        }
        App.setVolume(App.guilds.getGuildSettings(iGuild).volume.toFloat(), iGuild)

        try {
            if (App.client.getVoiceChannelByID(lastChannel) != null && lastChannel != "")
                App.client.getVoiceChannelByID(lastChannel).join()
        } catch (e: MissingPermissionsException) {
            e.printStackTrace()
        }

        if (queue!!.size > 0) {
            var source: AudioSource
            for (url in queue!!) {
                try {
                    if (url.contains("youtube"))
                        source = YouTubeAudio(url)
                    else if (url.contains("soundcloud"))
                        continue // TODO fix soundcloud
                    else
                        source = AudioStream(url)
                    audioPlayer.queue(source.getAudioTrack(null, false))
                } catch (e: NotStreamableException) {
                    e.printStackTrace()
                } catch (e: YouTubeAPIException) {
                    e.printStackTrace()
                } catch (e: UnirestException) {
                    e.printStackTrace()
                } catch (e: YouTubeDLException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: FFMPEGException) {
                    e.printStackTrace()
                } catch (e: UnsupportedAudioFileException) {
                    e.printStackTrace()
                }

            }
        }
        return this
    }

    val audioPlayer: AudioPlayer
        get() = AudioPlayer.getAudioPlayerForGuild(iGuild)

    @Throws(IOException::class, UnsupportedAudioFileException::class)
    fun playAudioFromAudioSource(source: AudioSource?, message: IMessage?, user: IUser) {
        if (source == null)
            return
        val player = AudioPlayer.getAudioPlayerForGuild(iGuild)
        if (message != null)
            editMessage(message, "Queuing **" + source.title + "**")
        try {
            if (source is YouTubeAudio && source.isLive) {
                if(message != null)
                    waitAndDeleteMessage(editMessage(message, user.mention() + " Could not queue **" + source.title + "**: Live Streams are currently not supported!"), 120)
                return
            } else if (source is YouTubeAudio && source.isDurationAnHour) {
                if(message != null)
                    waitAndDeleteMessage(editMessage(message, user.mention() + " Could not queue **" + source.title + "**: Video length must be less than 1 hour!"), 120)
                return
            }
            player.queue(source.getAudioTrack(user, announce))
            App.joinChannel(user, iGuild)
            if (message != null)
                waitAndDeleteMessage(editMessage(message, user.mention() + " Queued **" + source.title + "**"), 30)
            return
        } catch (e: YouTubeDLException) {
            e.printStackTrace()
            if (message != null)
                waitAndDeleteMessage(editMessage(message, user.mention() + " Could not queue **" + source.title + "**: An error occurred while downloading the video."), 120)
            return
        } catch (e: FFMPEGException) {
            e.printStackTrace()
            if (message != null)
                waitAndDeleteMessage(editMessage(message, user.mention() + " Could not queue **" + source.title + "**: An error occurred while converting to audio stream"), 120)
            return
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: MissingPermissionsException) {
            e.printStackTrace()
        }

        if (message != null)
            waitAndDeleteMessage(editMessage(message, user.mention() + " Could not queue **" + source.title + "**: (unknown reason)"), 120)
        return
    }
}