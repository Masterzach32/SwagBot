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
import net.masterzach32.commands4j.Command
import net.masterzach32.commands4j.Permission
import net.masterzach32.commands4j.getBotLockedMessage
import net.masterzach32.commands4j.getWrongArgumentsMessage
import net.masterzach32.commands4j.MetadataMessageBuilder
import net.masterzach32.swagbot.App
import net.masterzach32.swagbot.music.LocalPlaylist
import net.masterzach32.swagbot.music.player.YouTubeAudio
import net.masterzach32.swagbot.utils.exceptions.YouTubeAPIException
import org.json.JSONObject
import sx.blah.discord.handle.obj.*
import sx.blah.discord.util.MissingPermissionsException
import java.util.*

class PlaylistCommand: Command("Playlist", "playlist", "plist") {

    override fun execute(cmdUsed: String, args: Array<String>, user: IUser, message: IMessage, channel: IChannel, permission: Permission): MetadataMessageBuilder? {
        val guild = App.guilds.getGuildSettings(channel.guild)
        if(guild.botLocked)
            return getBotLockedMessage(channel)
        if(args.isEmpty())
            return getWrongArgumentsMessage(channel, this, cmdUsed)
        val perms = permission.ordinal >= Permission.MOD.ordinal
        val action = args[0]
        val manager = guild.playlistManager
        val builder = MetadataMessageBuilder(channel)
        // moderator actions
        if (action == "load" && perms) {
            val msg = MetadataMessageBuilder(channel).withContent("Loading playlists from save...").build()
            manager.load()
            msg.edit("${user.mention()} Finished loading all playlists.")
            return null
        } else if (action == "save" && perms) {
            manager.save()
            return builder.withContent("Saved all playlists.")
        } else if (action == "list") {
            return builder.withContent("**Playlists**: $manager")
        }
        // now we need a playlist name
        if (args.size < 2)
            return getWrongArgumentsMessage(channel, this, cmdUsed)
        val name = args[1]
        val playlist = manager[name]
        if (action == "create") {
            manager.add(LocalPlaylist(name, false, false))
            return builder.withContent("Created playlist **$name**")
        } else if (action == "delete" && perms) {
            if(manager.remove(name))
                return builder.withContent("Removed playlist **$name**")
            else
                return builder.withContent("Could not find playlist **$name**")
        } else if (action == "info") {
            if (playlist != null)
                return builder.withContent(playlist.getInfo())
            else
                builder.withContent("Could not find playlist **$name**")
        } else if (action == "queue") {
            if(playlist != null) {
                playlist.queue(user, guild)
                return builder.withContent("Queued playlist $name")
            } else
                return builder.withContent("Could not find playlist **$name**")
        } else if (action == "lock") {
            playlist?.toggleLocked()
            if (playlist != null)
                return builder.withContent(if (playlist.isLocked) "**$name** is now locked." else "**$name** is no longer locked.")
        }
        // now we need a link
        if (args.size < 3)
            return getWrongArgumentsMessage(channel, this, cmdUsed)
        if (action == "import") {
            var id: String? = null
            if (args[2].contains("playlist")) {
                id = args[2].substring(args[2].indexOf("list=") + 5)
            } else if (args[2].contains("list=")) {
                val link = args[2]
                val parts = link.split("&".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
                for (str in parts)
                    if (str.contains("list="))
                        id = str.replace("list=", "")
            }
            if (id != null) {
                try {
                    manager.add(LocalPlaylist(name, getYouTubeVideosFromPlaylist(id), false, false))
                    return builder.withContent("Imported YouTube playlist <${args[2]}> into playlist **$name**")
                } catch (e: YouTubeAPIException) {
                    e.printStackTrace()
                    return builder.withContent("Could not import <${args[2]}>")
                }
            } else
                return builder.withContent("Could not get playlist id from <" + args[2] + ">. Please let the developer know!")
        } else if (action == "add") {
            if (!args[2].contains("youtube") && !args[2].contains("soundcloud"))
                return builder.withContent("You must provide a YouTube or SoundCloud link!")
            else if (playlist != null) {
                message.delete()
                val s = playlist.add(args[2])
                return builder.withContent(if (s != null) "Added **" + s.title + "** to **${playlist.name}**" else "Could not add <${args[2]}> to playlist **$name**")
            }
        } else if (action == "remove" && perms) {
            if (playlist != null) {
                if (playlist.isLocked)
                    return builder.withContent("**$name** is locked!")
                playlist.remove(args[2])
                return builder.withContent("Removed <${args[2]}> from **")
            }
        }
        return null
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {
        usage.put("<action> <playlist name> [link/param]", "Basic parameters.")
        usage.put("create <playlist name>", "Create a new playlist.")
        usage.put("import <playlist name> <youtube playlist link>", "Import a playlist from youtube.")
        usage.put("add <playlist name> <youtube/soundcloud link>", "Add a song to the playlist.")
        usage.put("remove <playlist name> <youtube/soundcloud link>", "Remove a song from the playlist.")
        usage.put("delete <playlist name>", "Delete a playlist.")
        usage.put("queue <playlist name>", "Add a playlist to the queue.")
        usage.put("list", "List all playlists.")
        usage.put("info <playlist name> [page number]", "List all songs in a playlist.")
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