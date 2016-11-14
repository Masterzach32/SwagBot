/*
    SwagBot-java
    Copyright (C) 2016 Zach Kozar

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
package net.masterzach32.swagbot.commands.music

import com.mashape.unirest.http.Unirest
import com.mashape.unirest.http.exceptions.UnirestException
import net.masterzach32.commands4j.*
import net.masterzach32.commands4j.util.MetadataMessageBuilder
import net.masterzach32.swagbot.App
import net.masterzach32.swagbot.music.player.AudioSource
import net.masterzach32.swagbot.music.player.SoundCloudAudio
import net.masterzach32.swagbot.music.player.YouTubeAudio
import net.masterzach32.swagbot.utils.Utils
import net.masterzach32.swagbot.utils.exceptions.NotStreamableException
import net.masterzach32.swagbot.utils.exceptions.YouTubeAPIException
import org.json.JSONObject
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.handle.obj.IUser
import sx.blah.discord.util.RequestBuffer
import java.io.IOException
import java.net.URLEncoder
import java.util.*
import javax.sound.sampled.UnsupportedAudioFileException


class PlayCommand : Command("Play Music", "play", "p") {

    override fun execute(cmdUsed: String, args: Array<String>, user: IUser, message: IMessage, channel: IChannel, permission: Permission): MetadataMessageBuilder? {
        val guild = App.guilds.getGuildSettings(message.guild)
        val builder = MetadataMessageBuilder(channel)
        if (guild.botLocked)
            return getBotLockedMessage(channel)
        if (args.isEmpty())
            return getWrongArgumentsMessage(channel, this, cmdUsed)
        var source: AudioSource? = null
        var msg: IMessage? = null
        try {
            if (args[0].contains("youtube")) {
                if (args[0].contains("playlist")) {
                    RequestBuffer.request { message.delete() }
                    msg = builder.withContent("Queuing playlist ${args[0]}").build()
                    val playlist = getYouTubeVideosFromPlaylist(args[0].substring(args[0].indexOf("list=") + 5))
                    for (music in playlist) {
                        try {
                            guild.playAudioFromAudioSource(music, null, user)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        } catch (e: UnsupportedAudioFileException) {
                            e.printStackTrace()
                        }
                    }
                    waitAndDeleteMessage(msg.edit("${user.mention()} Queued playlist ${args[0]}"), 30)
                } else if (args[0].contains("list=")) {
                    val link = args[0]
                    val parts = link.split("&".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
                    var id: String? = null
                    parts.filter { it.contains("list=") }
                            .forEach { id = it.replace("list=", "") }
                    message.delete()
                    msg = builder.withContent("Queuing Playlist ${args[0]}").build()
                    if (id != null)
                        for (music in getYouTubeVideosFromPlaylist(id!!))
                            try {
                                guild.playAudioFromAudioSource(music, null, message.author)
                            } catch (e: IOException) {
                                e.printStackTrace()
                            } catch (e: UnsupportedAudioFileException) {
                                e.printStackTrace()
                            }
                    waitAndDeleteMessage(editMessage(msg, "${message.author} Queued playlist ${args[0]}"), 30)
                } else {
                    source = YouTubeAudio(args[0])
                }
            } else if (args[0].contains("soundcloud")) {
                return builder.withContent("Soundcloud tracks have been temporarly disabled.")//source = SoundCloudAudio(args[0])
            } else if (args[0].contains("iheart") || args[0].contains("shoutcast") || args[0].contains("spotify")) {
                return builder.withContent("SwagBot currently doesn't support that streaming service due to copyright reasons.")
            } else if ((args[0].startsWith("http://") || args[0].startsWith("https://")) && args[0].substring(args.size - 3) == "mp3") {

            } else {
                msg = builder.withContent("Searching Youtube...").build()
                val search = URLEncoder.encode(Utils.getContent(args, 0), "UTF-8")
                source = getVideoFromSearch(search)
                if (source == null)
                    editMessage(msg, "${message.author} I couldn't find a video for **${Utils.getContent(args, 0)}**, try searching for the artist's name and song title.")
            }
        } catch (e: NotStreamableException) {
            return builder.withContent("The track you queued cannot be streamed: ${e.url}").setAutoDelete(30)
        } catch (e: YouTubeAPIException) {
            return builder.withContent("Your video cannot be listed because it may be listed as private or not available in the region for SwagBot's server.").setAutoDelete(30)
        }
        try {
            message.delete()
            if(msg == null)
                msg = builder.withContent("Queuing...").build()
            else
                editMessage(msg, "Queuing...")
            guild.playAudioFromAudioSource(source, msg, user)
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: UnsupportedAudioFileException) {
            e.printStackTrace()
        }
        return null
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("<link>", "Play a YouTube video, SoundCloud track, or direct audio file.")
        usage.put("<search query>", "Search youtube for a song and play it.")
    }

    @Throws(UnirestException::class, YouTubeAPIException::class)
    private fun getYouTubeVideosFromPlaylist(id: String): List<YouTubeAudio> {
        val music = ArrayList<YouTubeAudio>()
        var response = Unirest.get("https://www.googleapis.com/youtube/v3/playlistItems?" +
                "part=contentDetails" +
                "&maxResults=50" +
                "&playlistId=" + id +
                "&key=" + App.prefs.googleAuthKey).asJson()
        var json = response.body.array.getJSONObject(0)
        var nextPage: String? = null
        if (json.has("nextPageToken"))
            nextPage = json.getString("nextPageToken")
        for (obj in json.getJSONArray("items"))
            if (obj is JSONObject)
                music.add(YouTubeAudio("https://www.youtube.com/watch?v=" + obj.getJSONObject("contentDetails").getString("videoId")))
        while (nextPage != null) {
            response = Unirest.get("https://www.googleapis.com/youtube/v3/playlistItems?" +
                    "part=contentDetails" +
                    "&maxResults=50" +
                    "&playlistId=" + id +
                    "&pageToken=" + nextPage +
                    "&key=" + App.prefs.googleAuthKey).asJson()
            json = response.body.array.getJSONObject(0)
            if (json.has("nextPageToken"))
                nextPage = json.getString("nextPageToken")
            else
                nextPage = null
            for (obj in json.getJSONArray("items"))
                if (obj is JSONObject)
                    music.add(YouTubeAudio("https://www.youtube.com/watch?v=" + obj.getJSONObject("contentDetails").getString("videoId")))
        }
        return music
    }

    @Throws(UnirestException::class, YouTubeAPIException::class)
    private fun getVideoFromSearch(search: String): YouTubeAudio? {
        val response = Unirest.get("https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=50&q=" + search + "&key=" + App.prefs.googleAuthKey).asJson()
        if (response.status != 200)
            return null
        val json: JSONObject
        json = response.body.array.getJSONObject(0)
        if (json.has("items") && json.getJSONArray("items").length() > 0 && json.getJSONArray("items").getJSONObject(0).has("id") && json.getJSONArray("items").getJSONObject(0).getJSONObject("id").has("videoId"))
            return YouTubeAudio("https://youtube.com/watch?v=" + json.getJSONArray("items").getJSONObject(0).getJSONObject("id").getString("videoId"))
        return null
    }
}