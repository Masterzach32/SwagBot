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
import sx.blah.discord.handle.obj.IUser
import sx.blah.discord.util.DiscordException
import sx.blah.discord.util.MissingPermissionsException
import sx.blah.discord.util.RequestBuffer
import sx.blah.discord.util.audio.AudioPlayer

import javax.sound.sampled.UnsupportedAudioFileException

class GuildSettings(@Transient val iGuild: IGuild, var commandPrefix: Char, var maxSkips: Int, var volume: Int, botLocked: Boolean, nsfwfilter: Boolean, private var announce: Boolean, private var changeNick: Boolean, var lastChannel: String?, private var queue: MutableList<String>?, val statusListener: StatusListener) {

    @Transient val playlistManager: PlaylistManager
    @Transient private val skipIDs: MutableList<String>
    private val guildName: String
    var isBotLocked: Boolean = false
        private set
    var isNSFWFilterEnabled: Boolean = false
        private set

    init {
        playlistManager = PlaylistManager(iGuild.id)
        skipIDs = ArrayList<String>()
        this.guildName = iGuild.name
        this.isBotLocked = botLocked
        this.isNSFWFilterEnabled = nsfwfilter
    }

    val id: String
        get() = iGuild.id

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

    fun toggleBotLocked() {
        isBotLocked = !isBotLocked
    }

    fun toggleNSFWFilter() {
        isNSFWFilterEnabled = !isNSFWFilterEnabled
    }

    fun shouldAnnounce(): Boolean {
        return announce
    }

    fun setShouldAnnounce(announce: Boolean) {
        this.announce = announce
    }

    fun shouldChangeNick(): Boolean {
        return changeNick
    }

    fun setChangeNick(changeNick: Boolean) {
        this.changeNick = changeNick
    }

    fun setQueue(queue: MutableList<String>) {
        this.queue = queue
    }

    fun getQueue(): MutableList<String>? {
        return queue
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
                        source = SoundCloudAudio(url)
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
}